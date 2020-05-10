@file:Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")

package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.coroutines.withLock
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

suspend inline fun <T : Any?> CoroutineScope.executeExInternal(
    interval: IDurationEx? = null,
    delayed: IDurationEx?,
    mutex: Mutex?,
    returnOnCatch: T,
    maxExecutions: Int,
    printStackTrace: Boolean,
    logStackTrace: Boolean,
    onCatch: suspend CoroutineScope.(t: Throwable) -> Unit,
    onFinally: suspend CoroutineScope.() -> Unit,
    retryDelay: IDurationEx?,
    coroutineContext: CoroutineContext,
    block: suspend CoroutineScope.(isRetry: Boolean) -> T
): T {
    val scope = this
    delayed?.also { delay(it) }
    if (!coroutineContext.isActive) return returnOnCatch

    var result: T = returnOnCatch
    do {
        val runtime = measureTimeMillis {
            mutex.withLock {
                catch(
                    Unit, maxExecutions, printStackTrace, logStackTrace,
                    onCatch = { throwable -> scope.onCatch(throwable) },
                    onFinally = { scope.onFinally() }) { isRetry ->
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

    return result
}

fun buildContext(
    coroutineContext: CoroutineContext,
    name: String?,
    isSuperVisionEnabled: Boolean
): CoroutineContext {
    var context = coroutineContext
    name?.also { CoroutineName(it) }
    if (isSuperVisionEnabled) context += SupervisorJob()
    return context
}