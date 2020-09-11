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
interface ITypeContinuation<T> {

    /**
     * Will suspend the current coroutine until [trigger] gets called.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun suspendUntilTrigger(
        wanted: T? = null,
        timeout: IDurationEx? = null
    ): T?

    /**
     * Triggers the continuation. Will launch a new coroutine if the underlying channel can not receive new values.
     *
     * @param value T
     */
    fun trigger(value: T)
}

open class TypeContinuation<T> : ITypeContinuation<T> {

    private val channel = LinkedList<Channel<T>>()

    override suspend fun suspendUntilTrigger(
        wanted: T?,
        timeout: IDurationEx?
    ): T = withTimeout(timeout) {
        var result: T
        val c = Channel<T>(Channel.UNLIMITED)
        channel += c
        do {
            result = c.receive()
        } while (wanted?.let { result != it } == true)
        channel -= c
        c.close()
        result
    }

    override fun trigger(value: T) = channel.runEach {
        if (!offer(value))
            Log.w("Could not trigger continuation because it was already triggered.")
    }.asUnit()
}