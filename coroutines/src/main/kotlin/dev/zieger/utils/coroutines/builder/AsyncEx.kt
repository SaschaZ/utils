package dev.zieger.utils.coroutines.builder

import dev.zieger.utils.globals.BuilderSettings.LOG_EXCEPTIONS
import dev.zieger.utils.globals.BuilderSettings.PRINT_EXCEPTIONS
import dev.zieger.utils.time.ITimeSpan
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * Wrapper for [async] with additional features:
 *
 * @param T Return type of [block] and type of [returnOnCatch].
 * @param returnOnCatch Return value when an exception was caught and [maxExecutions] were reached or [maxExecutions] is
 *  smaller than 1. See [include] and [exclude] for more details.
 * @param coroutineContext [CoroutineContext] to use for the [async] call.
 * @param start [CoroutineStart] parameter to use for the [async] call.
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
 * @param include Caught [Throwable] must be one of this classes or it is thrown again. Defaulting to a list of
 *  [Throwable].
 * @param exclude Caught [Throwable] must not be one this classes or it is thrown again. Defaulting to a list of
 *  [CancellationException].
 * @param useSuperVisorJob If ´true´, a SuperVisorJob is added to the used CoroutineContext. Defaulting to `false`.
 * @param onCatch Is always called for every matching [Throwable]. Defaulting to empty lambda.
 * @param onFinally Is called at the end of every execution.
 * @param block Lambda to execute inside the [async] call.
 *
 * @return Result of [block] or [returnOnCatch] when [maxExecutions] was reached.
 */
@Suppress("DeferredIsResult")
fun <T : Any?> CoroutineScope.asyncEx(
    returnOnCatch: T,
    coroutineContext: CoroutineContext = this.coroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    delayed: ITimeSpan? = null,
    maxExecutions: Int = 1,
    retryDelay: ITimeSpan? = null,
    timeout: ITimeSpan? = null,
    mutex: Mutex? = null,
    printStackTrace: Boolean = PRINT_EXCEPTIONS,
    logStackTrace: Boolean = LOG_EXCEPTIONS,
    name: String? = null,
    include: List<KClass<out Throwable>> = listOf(Throwable::class),
    exclude: List<KClass<out Throwable>> = listOf(CancellationException::class),
    useSuperVisorJob: Boolean = false,
    onCatch: suspend CoroutineScope.(t: Throwable) -> Unit = {},
    onFinally: suspend CoroutineScope.() -> Unit = {},
    block: suspend CoroutineScope.(numExecution: Int) -> T
): Deferred<T> = buildContext(coroutineContext, useSuperVisorJob, name).let { ctx ->
    async(ctx, start) {
        executeExInternal(
            ctx, returnOnCatch, null, delayed, mutex, maxExecutions, retryDelay, timeout,
            printStackTrace, logStackTrace, include, exclude, onCatch, onFinally, block
        )
    }
}
