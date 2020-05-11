package dev.zieger.utils.misc

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.coroutines.logToFile
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CancellationException

@Suppress("UNCHECKED_CAST")
inline fun <T> catch(
    returnOnCatch: T?,
    maxExecutions: Int = 1,
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    crossinline onCatch: (Throwable) -> Unit = {},
    crossinline onFinally: () -> Unit = {},
    block: (isRetry: Boolean) -> T
): T {
    var result: T
    var succeed = false

    (0 until maxExecutions).forEach { retryIndex ->
        result = try {
            block(retryIndex > 0).also { succeed = true }
        } catch (throwable: Throwable) {
            if (throwable !is CancellationException) {
                succeed = false
                onCatch(throwable)
                if (printStackTrace) {
                    System.err.println("${throwable.javaClass.simpleName}: ${throwable.message}")
                    throwable.printStackTrace()
                }
                if (logStackTrace) DefaultCoroutineScope().logToFile(throwable)
            }
            returnOnCatch ?: null as T
        } finally {
            if (succeed || retryIndex == maxExecutions - 1)
                onFinally()
        }

        if (succeed)
            return result
    }

    return returnOnCatch ?: null as T
}