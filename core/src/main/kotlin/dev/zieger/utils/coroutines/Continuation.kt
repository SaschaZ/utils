package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.*


/**
 * Allows to suspend until a method is called.
 */
interface IContinuationBase<T> {

    val scope: CoroutineScope

    /**
     * Will suspend the current coroutine until [trigger] gets called.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun suspendUntilTrigger(wanted: T? = null, timeout: IDurationEx? = null): T

    suspend fun suspendUntilTrigger(timeout: IDurationEx? = null): T = suspendUntilTrigger(null, timeout)

    /**
     * Triggers the continuation.
     */
    suspend fun trigger(value: T)

    fun CoroutineScope.triggerAndForget(value: T) = launch { trigger(value) }

    fun triggerAndForget(value: T) = scope?.launch { trigger(value) }
        ?: throw IllegalStateException("Using triggerAndForget call without any CoroutineScope defined.")
}

class Continuation(override val scope: CoroutineScope = DefaultCoroutineScope()) : IContinuationBase<Unit> {

    private val channel = LinkedList<Channel<Boolean>>()
    private val mutex = Mutex()

    override suspend fun suspendUntilTrigger(wanted: Unit?, timeout: IDurationEx?) = withTimeout(timeout) {
        val c = Channel<Boolean>()
        mutex.withLock {
            channel += c
        }
        c.receive()
    }.asUnit()

    override suspend fun trigger(value: Unit) = mutex.withLock {
        channel.runEach {
            send(true)
            close()
        }
        channel.clear()
    }.asUnit()

    suspend fun trigger() = trigger(Unit)

    fun triggerAndForget(scope: CoroutineScope) = scope.launch { trigger() }

    fun triggerAndForget() = scope.launch { trigger() }
}

