package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.coroutines.withTimeout
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
    coroutineContext: CoroutineContext,
    interval: IDurationEx? = null,
    delayed: IDurationEx? = null,
    mutex: Mutex? = null,
    returnOnCatch: T,
    maxExecutions: Int = Int.MAX_VALUE,
    retryDelay: IDurationEx? = null,
    timeout: IDurationEx? = null,
    printStackTrace: Boolean = true,
    logStackTrace: Boolean = false,
    onCatch: (suspend CoroutineScope.(t: Throwable) -> Unit)? = null,
    onFinally: (suspend CoroutineScope.() -> Unit)? = null,
    block: suspend CoroutineScope.(isRetry: Boolean) -> T = { returnOnCatch }
): T {
    delayed?.also { delay(it) }
    if (!coroutineContext.isActive) return returnOnCatch

    var result: T = returnOnCatch
    withTimeout(timeout) {
        do {
            val runtime = measureTimeMillis {
                (mutex ?: Mutex()).withLock {
                    val scope = CoroutineScope(coroutineContext)
                    catch(
                        Unit, maxExecutions, printStackTrace, logStackTrace,
                        onCatch = { throwable -> onCatch?.also { scope.launch { onCatch(throwable) } } },
                        onFinally = { if (onFinally != null) scope.launch { scope.onFinally() } }) { isRetry ->
                        if (isRetry) retryDelay?.also { delay(it) }
                        result = scope.block(isRetry)
                    }
                }
            }

            if (coroutineContext.isActive && interval != null) {
                val intervalDiff = interval - runtime
                if (intervalDiff.notZero) delay(intervalDiff)
            }
        } while (interval != null && coroutineContext.isActive)
    }

    return result
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