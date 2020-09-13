package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.base.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

/**
 * Allows to suspend until a method is called.
 */
interface ITypeContinuation<T> {

    /**
     * Will suspend the current coroutine until [trigger] or [triggerS] gets called.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun suspendUntilTrigger(
        wanted: T? = null,
        timeout: IDurationEx? = null
    ): T?

    /**
     * Triggers the continuation. Suspends if necessary.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun triggerS(value: T, timeout: IDurationEx? = null)

    /**
     * Triggers the continuation. Will launch a new coroutine if the underlying channel can not receive new values.
     *
     * @param timeout When the new launched coroutine is suspending longer than defined in [timeout] a
     * [TimeoutCancellationException] is thrown. If `null` no timeout is used. Defaulting to `null`.
     */
    fun trigger(value: T, timeout: IDurationEx? = null)
}

open class TypeContinuation<T>(
    private val channel: Channel<T> = Channel(),
    private val scope: CoroutineScope = DefaultCoroutineScope()
) : ITypeContinuation<T> {

    override suspend fun suspendUntilTrigger(
        wanted: T?,
        timeout: IDurationEx?
    ): T? = withTimeout(timeout) {
        var result: T = channel.receive()
        while (wanted?.let { result != it } == true) result = channel.receive()
        result
    }

    override suspend fun triggerS(value: T, timeout: IDurationEx?) =
        withTimeout(timeout) { channel.send(value) }.asUnit()

    override fun trigger(value: T, timeout: IDurationEx?) =
        if (!channel.offer(value)) scope.launchEx { triggerS(value, timeout) }.asUnit() else Unit
}

/**
 * Allows to suspend until a method is called.
 */
interface IContinuation {

    /**
     * Will suspend the current coroutine until [trigger] or [triggerS] gets called.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun suspendUntilTrigger(timeout: IDurationEx? = null)

    /**
     * Triggers the continuation. Suspends if necessary.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun triggerS(timeout: IDurationEx? = null)

    /**
     * Triggers the continuation. Will launch a new coroutine if the underlying channel can not receive new values.
     *
     * @param timeout When the new launched coroutine is suspending longer than defined in [timeout] a
     * [TimeoutCancellationException] is thrown. If `null` no timeout is used. Defaulting to `null`.
     */
    fun trigger(timeout: IDurationEx? = null)
}

class Continuation(
    private val scope: CoroutineScope? = null
) : IContinuation {

    private val channel = LinkedList<Channel<Boolean>>()

    override suspend fun suspendUntilTrigger(timeout: IDurationEx?) = withTimeout(timeout) {
        val c = Channel<Boolean>()
        channel += c
        c.receive()
        channel -= c
        c.close()
    }.asUnit()

    override suspend fun triggerS(timeout: IDurationEx?) =
        withTimeout(timeout) { LinkedList(channel).runEach { send(true) } }.asUnit()

    override fun trigger(timeout: IDurationEx?) =
        LinkedList(channel).runEach {
            if (!offer(true)) scope?.launch { send(true) }
                ?: Log.w("Could not trigger continuation because coroutine scope is not defined.")
        }.asUnit()
}

