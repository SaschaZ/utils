package de.gapps.utils.coroutines.builder

import de.gapps.utils.UtilsSettings.LOG_EXCEPTIONS
import de.gapps.utils.UtilsSettings.PRINT_EXCEPTIONS
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.time.duration.IDurationEx
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
    mutex: Mutex? = null,
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    name: String? = null,
    isSuperVisionEnabled: Boolean = false,
    onCatch: (suspend CoroutineScope.(t: Throwable) -> Unit)? = null,
    onFinally: (suspend CoroutineScope.() -> Unit)? = null,
    block: suspend CoroutineScope.(isRetry: Boolean) -> Unit
) = coroutineScope.launchEx(
    coroutineScope.coroutineContext,
    interval,
    delayed,
    maxExecutions,
    retryDelay,
    mutex,
    printStackTrace,
    logStackTrace,
    name,
    isSuperVisionEnabled,
    onCatch,
    onFinally,
    block
)

fun CoroutineScope.launchEx(
    coroutineContext: CoroutineContext = this.coroutineContext,
    interval: IDurationEx? = null,
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
    block: suspend CoroutineScope.(isRetry: Boolean) -> Unit
) = launch(buildContext(coroutineContext, name, isSuperVisionEnabled)) {
    executeExInternal(
        interval, delayed, mutex, Unit, maxExecutions, printStackTrace, logStackTrace,
        onCatch, onFinally, retryDelay, coroutineContext, block
    )
}