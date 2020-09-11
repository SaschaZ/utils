package dev.zieger.utils.coroutines

import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Same as [kotlinx.coroutines.withTimeout] but will execute the provided [block] without any timeout if [timeout] is
 * `null`.
 *
 * @param message When a [TimeoutCancellationException] is caught, a new [CancellationException] with the message
 *  provided by [message] is thrown.
 */
suspend inline fun <T> withTimeout(
    timeout: IDurationEx?,
    crossinline message: (Throwable) -> Any = { throwable ->
        "Timeout after $timeout with ${throwable.message}"
    },
    crossinline block: suspend () -> T
): T = timeout?.let {
    try {
        kotlinx.coroutines.withTimeout(timeout.millis) { block() }
    } catch (throwable: Throwable) {
        if (throwable is TimeoutCancellationException)
            throw CancellationException("${message(throwable)}")
        throw throwable
    }
} ?: block()