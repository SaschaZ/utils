package dev.zieger.utils.coroutines

import dev.zieger.utils.misc.ifNull
import dev.zieger.utils.time.duration.IDurationEx

suspend inline fun withTimeout(
    timeout: IDurationEx?,
    crossinline block: suspend () -> Unit
) {
    timeout?.let {
        kotlinx.coroutines.withTimeout(timeout.millis) { block() }
    } ifNull { block() }
}