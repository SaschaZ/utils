package dev.zieger.utils.coroutines

import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import java.util.*


/**
 * Allows to suspend until a method is called.
 */
interface IContinuationBase<T> {

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
    fun trigger(value: T)
}

class Continuation : IContinuationBase<Unit> {

    private var channel = LinkedList<Channel<Boolean>>()

    override suspend fun suspendUntilTrigger(wanted: Unit?, timeout: IDurationEx?) = withTimeout(timeout) {
        val c = Channel<Boolean>()
        channel.add(c)
        c.receive()
    }.asUnit()

    override fun trigger(value: Unit) {
        val tmp = channel
        channel = LinkedList()

        tmp.runEach {
            offer(true)
            close()
        }
    }

    fun trigger() = trigger(Unit)
}