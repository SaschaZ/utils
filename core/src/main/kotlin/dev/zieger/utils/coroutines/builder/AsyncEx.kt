package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.time.base.IDurationEx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

@Suppress("DeferredIsResult")
fun <T : Any?> CoroutineScope.asyncEx(
    returnOnCatch: T,
    coroutineContext: CoroutineContext = this.coroutineContext,
    delayed: IDurationEx? = null,
    maxExecutions: Int = 1,
    retryDelay: IDurationEx? = null,
    timeout: IDurationEx? = null,
    mutex: Mutex? = null,
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    name: String? = null,
    isSuperVisionEnabled: Boolean = false,
    include: List<KClass<out Throwable>> = listOf(Throwable::class),
    exclude: List<KClass<out Throwable>> = listOf(CancellationException::class),
    onCatch: suspend CoroutineScope.(t: Throwable) -> Unit = {},
    onFinally: suspend CoroutineScope.() -> Unit = {},
    block: suspend CoroutineScope.(numExecution: Int) -> T
): Deferred<T> = async(buildContext(coroutineContext, name, isSuperVisionEnabled)) {
    executeExInternal(
        coroutineContext, returnOnCatch, null, delayed, mutex, maxExecutions, retryDelay, timeout,
        printStackTrace, logStackTrace, include, exclude, onCatch, onFinally, block
    )
}
