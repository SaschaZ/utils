package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.time.duration.IDurationEx
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
    timeout: IDurationEx? = null,
    mutex: Mutex? = null,
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    name: String? = null,
    isSuperVisionEnabled: Boolean = false,
    onCatch: suspend CoroutineScope.(t: Throwable) -> Unit = {},
    onFinally: suspend CoroutineScope.() -> Unit = {},
    block: suspend CoroutineScope.(isRetry: Boolean) -> T
): T = withContext(
    buildContext(
        context ?: coroutineContext,
        name,
        isSuperVisionEnabled
    )
) {
    executeExInternal(
        coroutineContext, resultOnCatch, null, delayed, mutex, maxExecutions, retryDelay, timeout,
        printStackTrace, logStackTrace, onCatch, onFinally, block
    )
}