package dev.zieger.utils.delegates

import dev.zieger.utils.delegates.OnChangedParamsWithParent.Companion.DEFAULT_RECENT_VALUE_BUFFER_SIZE
import dev.zieger.utils.misc.DataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

interface IOnChangedParamsWithParent<P : Any?, T : Any?> {
    val initial: T
    val previousValueSize: Int
    val notifyForInitial: Boolean
    val notifyOnChangedValueOnly: Boolean
    val scope: CoroutineScope?
    val mutex: Mutex
    val safeSet: Boolean
    val veto: (T) -> Boolean
    val map: (T) -> T
    val onChangedS: (suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit)?
    val onChanged: (IOnChangedScopeWithParent<P, T>.(T) -> Unit)?
}

/**
 * @property initial The observed property will be initialized with this value.
 * @property scope [CoroutineScope] that is used to notify listener that requested to be notified within a coroutine.
 * Default is `null`.
 * @param storeRecentValues If set to `true` all values of the property will be stored and provided within the
 * [IOnChangedScopeWithParent]. Should be set to `false` when the values of the property consume too much memory.
 * Defaulting is `false`.
 * @property previousValueSize Size of the fifo used to store the recent values. Will be set to
 * [DEFAULT_RECENT_VALUE_BUFFER_SIZE] if [storeRecentValues] is `true`.
 * @property notifyForInitial When `true` a new listener will immediately notified for the initial value of the
 * property without the need of a change. Default is `false`.
 * @property notifyOnChangedValueOnly When `false` listener are only notified when the value of the property changed.
 * When `true` every "set" to the property will notify the listener. Default is `true`.
 * @property mutex If not `null` the [Mutex] will wrap the whole execution of [scope]. Default is `null`.
 * @property safeSet When `true`
 * @property veto Is invoked before every change of the property. When returning `true` the new value is not assigned
 * to the property. (Optional)
 * @property map Maps the new input value to the internal property. Is called after `veto` and before `onChanged`.
 * @property onChangedS Suspend on change callback. Only is invoked when [scope] is set. (Optional)
 * @property onChanged Unsuspended on change callback. Will be called immediately when a new value is set. (Optional)
 */
open class OnChangedParamsWithParent<P : Any?, T : Any?>(
    override val initial: T,
    override val scope: CoroutineScope? = null,
    storeRecentValues: Boolean = false,
    override val previousValueSize: Int = if (storeRecentValues) DEFAULT_RECENT_VALUE_BUFFER_SIZE else 0,
    override val notifyForInitial: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val mutex: Mutex = Mutex(),
    override val safeSet: Boolean = false,
    override val veto: (T) -> Boolean = { false },
    override val map: (T) -> T = { it },
    override val onChangedS: (suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null,
    override val onChanged: (IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null
) : DataClass(), IOnChangedParamsWithParent<P, T> {

    companion object {

        const val DEFAULT_RECENT_VALUE_BUFFER_SIZE = 100
    }
}

typealias OnChangedParams<T> = OnChangedParamsWithParent<Any?, T>