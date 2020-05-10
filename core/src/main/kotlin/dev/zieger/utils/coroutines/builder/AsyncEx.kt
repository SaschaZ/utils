package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

@Suppress("DeferredIsResult")
inline fun <T : Any?> CoroutineScope.asyncEx(
    returnOnCatch: T,
    coroutineContext: CoroutineContext = this.coroutineContext,
    delayed: IDurationEx? = null,
    maxExecutions: Int = 1,
    retryDelay: IDurationEx? = null,
    mutex: Mutex? = null,
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    name: String? = null,
    isSuperVisionEnabled: Boolean = false,
    crossinline onCatch: suspend CoroutineScope.(t: Throwable) -> Unit = {},
    crossinline onFinally: suspend CoroutineScope.() -> Unit = {},
    crossinline block: suspend CoroutineScope.(isRetry: Boolean) -> T
): Deferred<T> = async(buildContext(coroutineContext, name, isSuperVisionEnabled)) {
    executeExInternal(
        null, delayed, mutex, returnOnCatch, maxExecutions, printStackTrace, logStackTrace,
        onCatch, onFinally, retryDelay, coroutineContext, block
    )
}
