package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.time.base.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

fun launchEx(
    coroutineScope: CoroutineScope = DefaultCoroutineScope(),
    interval: IDurationEx? = null,
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
    block: suspend CoroutineScope.(isRetry: Boolean) -> Unit
) = coroutineScope.launchEx(
    coroutineScope.coroutineContext, interval, delayed, maxExecutions, retryDelay, timeout, mutex,
    printStackTrace, logStackTrace, name, isSuperVisionEnabled, onCatch, onFinally, block
)

fun CoroutineScope.launchEx(
    coroutineContext: CoroutineContext = this.coroutineContext,
    interval: IDurationEx? = null,
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
    block: suspend CoroutineScope.(isRetry: Boolean) -> Unit
) = launch(buildContext(coroutineContext, name, isSuperVisionEnabled)) {
    executeExInternal(
        coroutineContext, Unit, interval, delayed, mutex, maxExecutions, retryDelay, timeout, printStackTrace,
        logStackTrace, onCatch, onFinally, block
    )
}