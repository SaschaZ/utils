@file:Suppress("unused")

package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.ContinuationResult.Success
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.castSafe
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel

sealed class ContinuationResult<T> {

    val isSuccess get() = this is Success<*>
    val isFail get() = this is Fail

    val successResult: T? get() = castSafe<Success<T>>()?.result

    data class Success<T>(val result: T) : ContinuationResult<T>()
    object Fail : ContinuationResult<Any?>()
}

/**
 * Allows to suspend until a method is called.
 */
interface ITypeContinuation<T> {

    /**
     * Will suspend the current coroutine until [trigger] or [triggerS] gets called.
     *
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     * @param wanted Suspend until the triggered [ContinuationResult] has the [wanted] value. Defaulting to `null`.
     *
     * @return The [ContinuationResult] that was passed to the [trigger] or [triggerS] call.
     */
    suspend fun suspend(timeout: IDurationEx? = null, wanted: T? = null): ContinuationResult<T>

    /**
     * Triggers the continuation with the  provided [ContinuationResult]. Suspends if necessary.
     *
     * @param result
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun triggerS(result: ContinuationResult<T>, timeout: IDurationEx? = null)

    /**
     * Triggers the continuation with the  provided [resultValue]. Suspends if necessary.
     *
     * @param resultValue
     * @param timeout When suspending longer than defined in [timeout] a [TimeoutCancellationException] is thrown.
     * If `null` no timeout is used. Defaulting to `null`.
     */
    suspend fun triggerS(resultValue: T, timeout: IDurationEx? = null) = triggerS(Success(resultValue), timeout)

    /**
     * Triggers the continuation with the  provided [ContinuationResult]. Will launch a new coroutine if the underlying channel can
     * not receive new values.
     *
     * @param result
     * @param timeout When the new launched coroutine is suspending longer than defined in [timeout] a
     * [TimeoutCancellationException] is thrown. If `null` no timeout is used. Defaulting to `null`.
     */
    fun trigger(result: ContinuationResult<T>, timeout: IDurationEx? = null)

    /**
     * Triggers the continuation with the  provided [resultValue]. Will launch a new coroutine if the underlying channel can
     * not receive new values.
     *
     * @param resultValue
     * @param timeout When the new launched coroutine is suspending longer than defined in [timeout] a
     * [TimeoutCancellationException] is thrown. If `null` no timeout is used. Defaulting to `null`.
     */
    fun trigger(resultValue: T, timeout: IDurationEx? = null) = trigger(Success(resultValue), timeout)
}

open class TypeContinuation<T>(
    private val channel: Channel<ContinuationResult<T>> = Channel(),
    private val scope: CoroutineScope = DefaultCoroutineScope()
) : ITypeContinuation<T> {

    override suspend fun suspend(timeout: IDurationEx?, wanted: T?): ContinuationResult<T> =
        withTimeout(timeout) {
            var result: ContinuationResult<T>
            do {
                result = channel.receive()
            } while (wanted != null && result.successResult != wanted)
            result
        }

    override suspend fun triggerS(result: ContinuationResult<T>, timeout: IDurationEx?) =
        withTimeout(timeout) { channel.send(result) }

    override fun trigger(result: ContinuationResult<T>, timeout: IDurationEx?) =
        if (!channel.offer(result)) scope.launchEx { triggerS(result, timeout) }.asUnit() else Unit
}

/**
 * Same as [ITypeContinuation] with the fixed type [Unit]. Can be used if there is no need to transfer a result
 * parameter from the trigger calls to the suspend call.
 */
interface IContinuation : ITypeContinuation<Unit> {
    suspend fun suspend(timeout: IDurationEx? = null)
    suspend fun triggerS(timeout: IDurationEx? = null)
    fun trigger(timeout: IDurationEx? = null)
}

open class Continuation : TypeContinuation<Unit>(), IContinuation {
    override suspend fun suspend(timeout: IDurationEx?) = suspend(timeout, Unit).asUnit()
    override suspend fun triggerS(timeout: IDurationEx?) = triggerS(Success(Unit), timeout)
    override fun trigger(timeout: IDurationEx?) = trigger(Success(Unit), timeout)
}
