package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

internal suspend fun <T : Any?> executeExInternal(
    interval: IDurationEx? = null,
    delayed: IDurationEx?,
    mutex: Mutex?,
    returnOnCatch: T,
    maxExecutions: Int,
    printStackTrace: Boolean,
    logStackTrace: Boolean,
    onCatch: (suspend CoroutineScope.(t: Throwable) -> Unit)?,
    onFinally: (suspend CoroutineScope.() -> Unit)?,
    retryDelay: IDurationEx?,
    coroutineContext: CoroutineContext,
    block: suspend CoroutineScope.(isRetry: Boolean) -> T
): T {
    delayed?.also { delay(it) }
    if (!coroutineContext.isActive) return returnOnCatch
    (mutex ?: Mutex()).withLock {
        if (!coroutineContext.isActive) return returnOnCatch

        var result: T = returnOnCatch
        do {
            val runtime = measureTimeMillis {
                val scope = CoroutineScope(coroutineContext)
                catch(
                    Unit, maxExecutions, printStackTrace, logStackTrace,
                    onCatch = { throwable -> onCatch?.also { scope.launch { onCatch(throwable) } } },
                    onFinally = { if (onFinally != null) scope.launch { scope.onFinally() } }) { isRetry ->
                    if (isRetry) retryDelay?.also { delay(it) }
                    result = scope.block(isRetry)
                }
            }

            if (coroutineContext.isActive && interval != null) {
                val intervalDiff = interval - runtime
                if (intervalDiff.notZero) delay(intervalDiff)
            }
        } while (interval != null && coroutineContext.isActive)

        return result
    }
}

internal fun buildContext(
    coroutineContext: CoroutineContext,
    name: String?,
    isSuperVisionEnabled: Boolean
): CoroutineContext {
    var context = coroutineContext
    name?.also { CoroutineName(it) }
    if (isSuperVisionEnabled) context += SupervisorJob()
    return context
}