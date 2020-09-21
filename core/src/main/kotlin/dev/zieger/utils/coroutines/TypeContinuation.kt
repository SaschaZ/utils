package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.base.IDurationEx
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

/**
 * Allows to suspend until [resume] with any wanted value is called.
 */
open class TypeContinuation<T> {

    companion object {
        sealed class ContinuationHolder<T> {
            data class Value<T>(val value: T) : ContinuationHolder<T>()
            data class Exception<T>(val throwable: Throwable) : ContinuationHolder<T>()
        }
    }

    protected open var channel = HashMap<T?, LinkedList<Channel<ContinuationHolder<T>>>>()
    protected open val addMutex = Mutex()

    /**
     * Amount [resume] was called.
     */
    protected open val resumedInternal = AtomicInteger(0)
    open val resumed get() = resumedInternal.get()
    open val resumedOnce get() = resumed > 0

    open var lastException: Throwable? = null
        protected set

    /**
     * Will suspend the current coroutine until [resume] gets called with the [wanted] value.
     *
     * @param wanted Suspend until [resume] is called with the [wanted] value. If `null` every triggered value will
     *   resume execution. Defaulting to `null`.
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     *   If `null` no timeout is used. Defaulting to `null`.
     */
    open suspend fun suspend(
        wanted: T? = null,
        timeout: IDurationEx? = null
    ): T = withTimeout(timeout) {
        val c = Channel<ContinuationHolder<T>>()
        addMutex.withLock { channel.getOrPut(wanted) { LinkedList() }.add(c) }
        c.receive().let { result ->
            @Suppress("UNCHECKED_CAST")
            when (result) {
                is ContinuationHolder.Value<*> -> result.value as T
                is ContinuationHolder.Exception -> throw result.throwable
            }
        }
    }

    /**
     * Will suspend the current coroutine until [resume] gets called.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     *   If `null` no timeout is used. Defaulting to `null`.
     */
    open suspend fun suspend(timeout: IDurationEx? = null): T = suspend(null, timeout)

    /**
     * Resumes all callers of [suspend] that used [value] or `null` as wanted value.
     */
    open fun resume(value: T) {
        resumedInternal.incrementAndGet()
        ((channel.remove(null) ?: emptyList()) + (channel.remove(value) ?: emptyList())).runEach {
            offer(ContinuationHolder.Value(value))
            close()
        }
    }

    /**
     * Resumes all callers of [suspend] and let them throw [exception].
     *
     * @param exception [Throwable] to throw when the caller of [suspend] is resumed.
     */
    open fun resumeWithException(exception: Throwable) {
        val tmp = channel
        channel = HashMap()

        lastException = exception
        resumedInternal.incrementAndGet()

        tmp.forEach { (_, value) ->
            value.forEach {
                it.offer(ContinuationHolder.Exception(exception))
                it.close()
            }
        }
    }
}

/**
 *
 */
suspend inline fun <T> suspend(
    wanted: T? = null,
    timeout: IDurationEx? = null,
    crossinline block: suspend (continuation: TypeContinuation<T>) -> T
): T = TypeContinuation<T>().let { cont -> launchEx { block(cont) }; cont.suspend(wanted, timeout) }