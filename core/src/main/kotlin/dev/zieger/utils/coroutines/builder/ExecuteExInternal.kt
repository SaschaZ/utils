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
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

internal suspend inline fun <T : Any?> executeExInternal(
    coroutineContext: CoroutineContext,
    returnOnCatch: T,
    interval: IDurationEx? = null,
    delayed: IDurationEx? = null,
    include: List<KClass<out Throwable>> = listOf(Throwable::class),
    exclude: List<KClass<out Throwable>> = listOf(CancellationException::class),
    mutex: Mutex? = null,
    maxExecutions: Int = Int.MAX_VALUE,
    retryDelay: IDurationEx? = null,
    timeout: IDurationEx? = null,
    printStackTrace: Boolean = true,
    logStackTrace: Boolean = false,
    crossinline onCatch: suspend CoroutineScope.(t: Throwable) -> Unit,
    crossinline onFinally: suspend CoroutineScope.() -> Unit,
    crossinline block: suspend CoroutineScope.(numExecution: Int) -> T
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
                        Unit, maxExecutions, include, exclude, printStackTrace, logStackTrace,
                        onCatch = { throwable -> scope.launch { onCatch(throwable) } },
                        onFinally = { scope.launch { scope.onFinally() } }) { numExecution ->
                        if (numExecution > 0) retryDelay?.also { delay(it) }
                        result = scope.block(numExecution)
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