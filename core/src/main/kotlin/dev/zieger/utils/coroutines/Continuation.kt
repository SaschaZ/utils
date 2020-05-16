package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.channels.Channel


interface IContinuation {

    val timeout: IDurationEx?

    suspend fun suspendUntilTrigger()

    /**
     * Will continue execution.
     * May be used by external components.
     */
    suspend fun trigger()
}

class Continuation(override val timeout: IDurationEx? = null) : IContinuation {

    internal val scope = DefaultCoroutineScope()
    private val channel = Channel<Boolean>()

    override suspend fun suspendUntilTrigger() = withTimeout(timeout) { channel.receive() }

    override suspend fun trigger() = channel.send(true)
}