package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.UtilsSettings.ERROR_LOG_FILE
import dev.zieger.utils.UtilsSettings.LOG_EXCEPTIONS
import dev.zieger.utils.UtilsSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

/**
 * Wrapper for [withContext] with additional features:
 *
 * @param T Return type of [block] and type of [returnOnCatch].
 * @param returnOnCatch Return value when an exception was caught and [maxExecutions] were reached or [maxExecutions] is
 *  smaller than 1. See [include] and [exclude] for more details.
 * @param context [CoroutineContext] to use for the [withContext] call.
 * @param delayed When not `null`, execution is delayed by the provided [IDurationEx]. Defaulting to `null`.
 * @param maxExecutions When greater `1` the [block] is executed again after an exception was caught but only for a
 *   maximum of [maxExecutions] times. When smaller than 1 the [block] is not executed and [returnOnCatch] is returned.
 *   Defaulting to `1`. See [include] and [exclude] for more details.
 * @param retryDelay When not `null`, the execution will suspend for the provided [IDurationEx] after an exception was
 *   caught and [maxExecutions] was not reached already. Defaulting to `null`.
 * @param timeout When not `null`, a [TimeoutCancellationException] is thrown when the execution of [block] takes longer
 *   than the defined [timeout]. Defaulting to `null`.
 * @param mutex When not `null`, the execution is wrapped into the provided [mutex]. Defaulting to `null`.
 * @param printStackTrace Print a stack trace for matching [Throwable]. Defaulting to [PRINT_EXCEPTIONS].
 * @param logStackTrace Append the stack trace to a file specified by [ERROR_LOG_FILE] for every matching [Throwable].
 *  Defaulting to [LOG_EXCEPTIONS].
 * @param name Name of the new coroutine. Defaulting to `null`.
 * @param isSuperVisionEnabled Defaulting to `false`.
 * @param include Caught [Throwable] must be one of this classes or it is thrown again. Defaulting to a list of
 *  [Throwable].
 * @param exclude Caught [Throwable] must not be one this classes or it is thrown again. Defaulting to a list of
 *  [CancellationException].
 * @param onCatch Is always called for every matching [Throwable]. Defaulting to empty lambda.
 * @param onFinally Is called at last when no [Throwable] was caught or [maxExecutions] was reached. Is not called,
 *  when the [Throwable] does not match the specified [include] and [exclude] classes. Defaulting to empty lambda.
 * @param block Lambda to execute inside the [withContext] call.
 *
 * @return Result of [block] or [returnOnCatch] when [maxExecutions] was reached.
 */
suspend fun <T : Any?> withContextEx(
    returnOnCatch: T,
    context: CoroutineContext? = null,
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
): T = buildContext(context ?: coroutineContext, name, isSuperVisionEnabled).let { ctx ->
    withContext(ctx) {
        executeExInternal(
            ctx, returnOnCatch, null, delayed, mutex, maxExecutions, retryDelay, timeout,
            printStackTrace, logStackTrace, include, exclude, onCatch, onFinally, block
        )
    }
}