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

@Suppress("UNCHECKED_CAST")
inline fun <T> catch(
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
            }
            returnOnCatch ?: null as T
        } finally {
            if (succeed || retryIndex == maxExecutions - 1)
                onFinally()
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