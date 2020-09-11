package dev.zieger.utils.coroutines

import dev.zieger.utils.log.Log
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import java.util.*


/**
 * Allows to suspend until a method is called.
 */
interface IContinuation {

    /**
     * Will suspend the current coroutine until [trigger] gets called.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun suspendUntilTrigger(timeout: IDurationEx? = null)

    /**
     * Triggers the continuation.
     */
    fun trigger()
}

class Continuation : IContinuation {

    private val channel = LinkedList<Channel<Boolean>>()

    override suspend fun suspendUntilTrigger(timeout: IDurationEx?) = withTimeout(timeout) {
        val c = Channel<Boolean>()
        channel += c
        c.receive()
        channel -= c
        c.close()
    }.asUnit()

    override fun trigger() = LinkedList(channel).runEach {
        if (!offer(true))
            Log.w("Could not trigger continuation because it was already triggered.")
    }.asUnit()
}

