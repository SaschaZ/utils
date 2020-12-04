package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.coroutines.TimeoutCancellationException
import dev.zieger.utils.coroutines.withLock
import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

internal suspend inline fun <T : Any?> executeExInternal(
    coroutineContext: CoroutineContext,
    returnOnCatch: T,
    interval: IDurationEx? = null,
    delayed: IDurationEx? = null,
    mutex: Mutex? = null,
    maxExecutions: Int = Int.MAX_VALUE,
    retryDelay: IDurationEx? = null,
    timeout: IDurationEx? = null,
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    include: List<KClass<out Throwable>> = listOf(Throwable::class),
    exclude: List<KClass<out Throwable>> = listOf(CancellationException::class),
    crossinline onCatch: suspend CoroutineScope.(t: Throwable) -> Unit,
    crossinline onFinally: suspend CoroutineScope.() -> Unit,
    crossinline block: suspend CoroutineScope.(numExecution: Int) -> T
): T {
    delayed?.also { delay(it) }
    if (!coroutineContext.isActive) return returnOnCatch

    var result: T = returnOnCatch
    catch(Unit, include = timeout?.let { listOf(TimeoutCancellationException::class) } ?: emptyList(),
        exclude = emptyList(), printStackTrace = false, logStackTrace = false, onCatch = { return returnOnCatch }) {
        withTimeout(timeout) {
            do {
                val runtime = measureTimeMillis {
                    mutex.withLock {
                        CoroutineScope(coroutineContext).run {
                            catch(
                                Unit, maxExecutions, printStackTrace = printStackTrace, logStackTrace = logStackTrace,
                                include = include, exclude = exclude,
                                onCatch = { throwable -> onCatch(throwable) },
                                onFinally = { onFinally() }) { numExecution ->
                                if (numExecution > 0) retryDelay?.also { delay(it) }
                                result = block(numExecution)
                            }
                        }
                    }
                }

                if (coroutineContext.isActive && interval != null) {
                    val intervalDiff = interval - runtime
                    if (intervalDiff.positive) delay(intervalDiff)
                }
            } while (interval != null && coroutineContext.isActive)
        }
    }

    return result
}

internal fun buildContext(
    coroutineContext: CoroutineContext,
    name: String?
): CoroutineContext {
    var context = coroutineContext
    name?.also { context += CoroutineName(it) }
    return context
}