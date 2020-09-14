package dev.zieger.utils.misc

import dev.zieger.utils.UtilsSettings.ERROR_LOG_FILE
import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.LOG_SCOPE
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

/**
 * Executes provided [block] inside a try-catch-finally statement.
 *
 * @param T Return type of [block] and type of [returnOnCatch].
 * @param returnOnCatch Return value when an exception was caught and [maxExecutions] were reached or [maxExecutions] is
 *  smaller than 1.
 * @param maxExecutions After an exception was caught the [block] is executed again for a maximum of [maxExecutions]
 *  times. When smaller than 1 the [block] is not executed and [returnOnCatch] is returned. Defaulting to 1.
 * @param include Caught [Throwable] must be one of this classes or it is thrown again. Defaulting to a list of
 *  [Throwable].
 * @param exclude Caught [Throwable] must not be one this classes or it is thrown again. Defaulting to a list of
 *  [CancellationException].
 * @param printStackTrace Print a stack trace for matching [Throwable]. Defaulting to [PRINT_EXCEPTIONS].
 * @param logStackTrace Append the stack trace to a file specified by [ERROR_LOG_FILE] for every matching [Throwable].
 *  Defaulting to [LOG_EXCEPTIONS].
 * @param onCatch Is always called for every matching [Throwable].
 * @param onFinally Is called at last when no [Throwable] was caught or [maxExecutions] was reached. Is not called,
 *  when the [Throwable] does not match the specified [include] and [exclude] classes.
 * @param block Lambda to execute into try-catch-finally statement.
 *
 * @return Result of [block] or [returnOnCatch] when [maxExecutions] was reached.
 */
inline fun <T : Any?> catch(
    returnOnCatch: T,
    maxExecutions: Int = 1,
    include: List<KClass<out Throwable>> = listOf(Throwable::class),
    exclude: List<KClass<out Throwable>> = listOf(CancellationException::class),
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    crossinline onCatch: (Throwable) -> Unit = {},
    crossinline onFinally: () -> Unit = {},
    block: (numExecution: Int) -> T
): T {
    var result: T
    var succeed = false

    (0 until maxExecutions).forEach { retryIndex ->
        result = try {
            block(retryIndex).also { succeed = true }
        } catch (throwable: Throwable) {
            if (include.any { it.isInstance(throwable) }
                && exclude.none { it.isInstance(throwable) }) {
                succeed = false
                onCatch(throwable)
                if (printStackTrace) {
                    System.err.println("${throwable.javaClass.simpleName}: ${throwable.message}")
                    throwable.printStackTrace()
                }
                if (logStackTrace) throwable.log()
                returnOnCatch
            } else {
                throw throwable
            }
        } finally {
            if (succeed) onFinally()
        }
        if (succeed) return result
    }

    onFinally()
    return returnOnCatch
}

private val errorLogMutex = Mutex()

fun Throwable.log() =
    LOG_SCOPE.launch {
        errorLogMutex.withLock {
            ERROR_LOG_FILE()?.also { file ->
                var log = "$currentTime: ${javaClass.simpleName}: $message\n"
                log += stackTrace.joinToString("\n")
                log += "\n\n\n"
                log += file.readText()
                file.writeText(log)
            }
        }
    }

private val currentTime: String
    get() = SimpleDateFormat("dd.MM.yyyy-HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis()))