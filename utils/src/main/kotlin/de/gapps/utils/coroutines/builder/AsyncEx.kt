package de.gapps.utils.coroutines.builder

import de.gapps.utils.UtilsSettings.LOG_EXCEPTIONS
import de.gapps.utils.UtilsSettings.PRINT_EXCEPTIONS
import de.gapps.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

@Suppress("DeferredIsResult")
fun <T : Any?> CoroutineScope.asyncEx(
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
    onCatch: (suspend CoroutineScope.(t: Throwable) -> Unit)? = null,
    onFinally: (suspend CoroutineScope.() -> Unit)? = null,
    block: suspend CoroutineScope.(isRetry: Boolean) -> T
): Deferred<T> = async(buildContext(coroutineContext, name, isSuperVisionEnabled)) {
    executeExInternal(
        null, delayed, mutex, returnOnCatch, maxExecutions, printStackTrace, logStackTrace,
        onCatch, onFinally, retryDelay, coroutineContext, block
    )
}
