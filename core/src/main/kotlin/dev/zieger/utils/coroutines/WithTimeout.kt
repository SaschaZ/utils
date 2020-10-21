package dev.zieger.utils.coroutines

import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CancellationException

/**
 * Same as [kotlinx.coroutines.withTimeout] but will execute the provided [block] directly if [timeout] is `null`.
 *
 * @param timeout Timeout to use. When `null`, [block] is executed directly without any timeout.
 * @param message When the timeout is reached, a [TimeoutCancellationException] with message returned in this lambda
 *   is thrown. Defaulting to "Timeout after $timeout with ${throwable.message}".
 * @param block Lambda to execute.
 */
suspend inline fun <T> withTimeout(
    timeout: IDurationEx?,
    crossinline message: (throwable: Throwable) -> Any = { throwable ->
        "Timeout after $timeout with ${throwable.message}"
    },
    crossinline block: suspend () -> T
): T = if (timeout != null) {
    catch(null, logStackTrace = false, printStackTrace = false, exclude = emptyList(),
        onCatch = { throwable ->
            if (throwable is kotlinx.coroutines.TimeoutCancellationException)
                throw TimeoutCancellationException("${message(throwable)}")
            throw throwable
        }) {
        kotlinx.coroutines.withTimeout(timeout.millis) { block() }
    }!! // will throw exception when result is null
} else block()

open class TimeoutCancellationException(message: String) : CancellationException(message)