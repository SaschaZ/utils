package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel


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
    suspend fun suspendUntilTrigger(timeout: IDurationEx? = null) {
    }

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
    private val channel: Channel<Boolean> = Channel(),
    private val scope: CoroutineScope = DefaultCoroutineScope()
) : IContinuation {

    override suspend fun suspendUntilTrigger(timeout: IDurationEx?) =
        withTimeout(timeout) { channel.receive() }.asUnit()

    override suspend fun triggerS(timeout: IDurationEx?) = withTimeout(timeout) { channel.send(true) }

    override fun trigger(timeout: IDurationEx?) =
        if (!channel.offer(true)) scope.launchEx { triggerS(timeout) }.asUnit() else Unit
}

