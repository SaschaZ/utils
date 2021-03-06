package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

/**
 * Wrapper for [launch] with additional features:
 *
 * @param interval
 * @param delayed
 * @param include
 * @param exclude
 * @param maxExecutions
 * @param retryDelay
 * @param timeout
 * @param mutex
 * @param printStackTrace
 * @param logStackTrace
 * @param name
 * @param isSuperVisionEnabled
 * @param onCatch
 * @param onFinally
 * @param block
 */
suspend fun launchEx(
    interval: IDurationEx? = null,
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
    block: suspend CoroutineScope.(numExecution: Int) -> Unit
): Job {
    val ctx = coroutineContext
    return object : CoroutineScope {
        override val coroutineContext: CoroutineContext = ctx
    }.launchEx(
        coroutineContext, interval, delayed, include, exclude, maxExecutions, retryDelay, timeout, mutex,
        printStackTrace, logStackTrace, name, isSuperVisionEnabled, onCatch, onFinally, block
    )
}

fun CoroutineScope.launchEx(
    coroutineContext: CoroutineContext = this.coroutineContext,
    interval: IDurationEx? = null,
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
    block: suspend CoroutineScope.(numExecution: Int) -> Unit
): Job = launch(buildContext(coroutineContext, name, isSuperVisionEnabled)) {
    executeExInternal(
        coroutineContext, Unit, interval, delayed, include, exclude, mutex, maxExecutions, retryDelay, timeout,
        printStackTrace, logStackTrace, onCatch, onFinally, block
    )
}