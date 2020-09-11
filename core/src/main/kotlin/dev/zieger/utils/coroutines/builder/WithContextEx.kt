package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

suspend fun <T : Any?> withContextEx(
    resultOnCatch: T,
    context: CoroutineContext? = null,
    delayed: IDurationEx? = null,
    include: List<KClass<out Throwable>> = listOf(Throwable::class),
    exclude: List<KClass<out Throwable>> = listOf(CancellationException::class),
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
    block: suspend CoroutineScope.(numExecution: Int) -> T
): T = withContext(
    buildContext(
        context ?: coroutineContext,
        name,
        isSuperVisionEnabled
    )
) {
    executeExInternal(
        coroutineContext, resultOnCatch, null, delayed, include, exclude, mutex, maxExecutions, retryDelay,
        timeout, printStackTrace, logStackTrace, onCatch, onFinally, block
    )
}