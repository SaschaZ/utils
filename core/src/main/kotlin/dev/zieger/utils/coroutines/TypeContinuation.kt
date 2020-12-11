package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.TypeContinuation.Companion.ContinuationHolder.Exception
import dev.zieger.utils.coroutines.TypeContinuation.Companion.ContinuationHolder.Value
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap
import kotlin.coroutines.coroutineContext

/**
 * Allows to suspend until [resume] with any wanted value is called.
 */
open class TypeContinuation<T : Any?> {

    companion object {
        sealed class ContinuationHolder<T> {
            data class Value<T>(val value: T) : ContinuationHolder<T>()
            data class Exception<T>(val throwable: Throwable) : ContinuationHolder<T>()
        }
    }

    protected open var channelMap = HashMap<T?, LinkedList<Channel<ContinuationHolder<T>>>>()
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
        addMutex.withLock { channelMap.getOrPut(wanted) { LinkedList() }.add(c) }
        c.receive().let { result ->
            @Suppress("UNCHECKED_CAST")
            when (result) {
                is Value<*> -> result.value as T
                is Exception -> throw result.throwable
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
        ((channelMap.remove(null) ?: emptyList<Channel<ContinuationHolder<T>>>()) +
                (channelMap.remove(value) ?: emptyList())).runEach {
            offer(Value(value))
            close()
        }
    }

    /**
     * Resumes all callers of [suspend] and let them throw [exception].
     *
     * @param exception [Throwable] to throw when the caller of [suspend] is resumed.
     */
    open fun resumeWithException(exception: Throwable) {
        val tmp = channelMap
        channelMap = HashMap()

        lastException = exception
        resumedInternal.incrementAndGet()

        tmp.forEach { (_, value) ->
            value.forEach {
                it.offer(Exception(exception))
                it.close()
            }
        }
    }
}

/**
 *
 */
suspend inline fun <T : Any> suspendCoroutine(
    wanted: T? = null,
    timeout: IDurationEx? = null,
    crossinline block: suspend TypeContinuation<T>.() -> Unit
): T = TypeContinuation<T>().run {
    CoroutineScope(coroutineContext).launchEx(onCatch = { resumeWithException(it) }) { block() }
    suspend(wanted, timeout)
}