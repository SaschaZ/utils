package de.gapps.utils.coroutines

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.ifN
import de.gapps.utils.time.duration.IDurationEx
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout


suspend inline fun withTimeout(
    timeout: IDurationEx?,
    crossinline block: suspend () -> Unit
) {
    timeout?.let {
        withTimeout(timeout.millis) { block() }
    } ifN { block() }
}

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