package de.gapps.utils.coroutines.builder

import de.gapps.utils.UtilsSettings.LOG_EXCEPTIONS
import de.gapps.utils.UtilsSettings.PRINT_EXCEPTIONS
import de.gapps.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

suspend fun <T : Any?> withContextEx(
    resultOnCatch: T,
    context: CoroutineContext? = null,
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
): T = withContext(
    buildContext(
        context ?: coroutineContext,
        name,
        isSuperVisionEnabled
    )
) {
    executeExInternal(
        null, delayed, mutex, resultOnCatch, maxExecutions, printStackTrace, logStackTrace,
        onCatch, onFinally, retryDelay, coroutineContext, block
    )
}