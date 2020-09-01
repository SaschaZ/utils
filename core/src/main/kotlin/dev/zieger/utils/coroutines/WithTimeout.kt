package dev.zieger.utils.coroutines

import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.base.IDurationEx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Same as [kotlinx.coroutines.withTimeout] but will execute the provided [block] without any timeout if [timeout] is
 * `null`.
 *
 * @param message When a [TimeoutCancellationException] is catched, a new [CancellationException] with the message
 *  provided by [message] is thrown.
 */
suspend inline fun <T> withTimeout(
    timeout: IDurationEx?,
    crossinline message: (Throwable) -> Any = { throwable ->
        "Timeout after $timeout with ${throwable.message}"
    },
    crossinline block: suspend () -> T
): T = if (timeout != null) {
    catch(null, logStackTrace = false, printStackTrace = false, onCatch = { throwable ->
        if (throwable is TimeoutCancellationException)
            throw CancellationException("${message(throwable)}")
        throw throwable
    }) {
        kotlinx.coroutines.withTimeout(timeout.millis) { block() }
    }!! // will throw exception is null
} else block()