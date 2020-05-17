package dev.zieger.utils.coroutines

import dev.zieger.utils.time.duration.IDurationEx

/**
 * Same as [kotlinx.coroutines.withTimeout] but will execute the provided [block] without any timeout if [timeout] is
 * `null`.
 */
//suspend inline fun withTimeout(
//    timeout: IDurationEx?,
//    crossinline block: suspend () -> Unit
//) = timeout?.let {
//    kotlinx.coroutines.withTimeout(timeout.millis) { block() }
//} ?: block()
suspend inline fun <T> withTimeout(
    timeout: IDurationEx?,
    crossinline block: suspend () -> T
): T = timeout?.let {
    kotlinx.coroutines.withTimeout(timeout.millis) { block() }
} ?: block()