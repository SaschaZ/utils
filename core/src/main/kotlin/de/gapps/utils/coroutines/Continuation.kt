package de.gapps.utils.coroutines

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
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

suspend fun continueWhen(
    timeout: IDurationEx? = null,
    block: suspend () -> Unit
) {
    val continuation = Continuation(timeout)
    block()
    continuation.suspendUntilTrigger()
}

interface IContinuation {

    val timeout: IDurationEx?

    suspend fun suspendUntilTrigger()

    /**
     * Will continue execution.
     * May be used by external components.
     */
    fun trigger()
}

class Continuation(override val timeout: IDurationEx? = null) : IContinuation {

    internal val scope = DefaultCoroutineScope()
    private val channel = Channel<Boolean>()

    override suspend fun suspendUntilTrigger() = withTimeout(timeout) { channel.receive() }

    override fun trigger() = scope.launchEx { channel.send(true) }.asUnit()
}