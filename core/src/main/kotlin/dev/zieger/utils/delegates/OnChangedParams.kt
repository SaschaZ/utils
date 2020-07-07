package dev.zieger.utils.delegates

import dev.zieger.utils.misc.DataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

/**
 * @property initial The observed property will be initialized with this value.
 * @property storeRecentValues If set to `true` all values of the property will be stored and provided within the
 * [IOnChangedScope2]. Should be set to `false` when the values of the property consume too much memory.
 * Defaulting is `false`.
 * @property notifyForInitial When `true` a new listener will immediately notified for the initial value of the
 * property without the need of a change. Default is `false`.
 * @property notifyOnChangedValueOnly When `false` listener are only notified when the value of the property changed.
 * When `true` every "set" to the property will notify the listener. Default is `true`.
 * @property scope [CoroutineScope] that is used to notify listener that requested to be notified within a coroutine.
 * Default is `null`.
 * @property mutex If not `null` the [Mutex] will wrap the whole execution of [scope]. Default is `null`.
 * @property veto Is invoked before every change of the property. When returning `true` the new value is not assigned
 * to the property. (Optional)
 * @property onChangedS Suspend on change callback. Only is invoked when [scope] is set. (Optional)
 * @property onChanged Unsuspended on change callback. Will be called immediately when a new value is set. (Optional)
 */
interface IOnChangedParams2<P : Any?, T : Any?> {
    val initial: T
    val recentValueSize: Int
    val notifyForInitial: Boolean
    val notifyOnChangedValueOnly: Boolean
    val scope: CoroutineScope?
    val mutex: Mutex?
    val scopeFactory: IScope2Factory<P, T>
    val veto: (T) -> Boolean
    val onChangedS: suspend IOnChangedScope2<P, T>.(T) -> Unit
    val onChanged: IOnChangedScope2<P, T>.(T) -> Unit
}

typealias IOnChangedParams<T> = IOnChangedParams2<Any?, T>

open class OnChangedParams2<P : Any?, T : Any?>(
    override val initial: T,
    storeRecentValues: Boolean = false,
    override val recentValueSize: Int = if (storeRecentValues) 100 else 0,
    override val notifyForInitial: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val scope: CoroutineScope? = null,
    override val mutex: Mutex? = null,
    override val scopeFactory: IScope2Factory<P, T> = OnChangedScope2Factory(),
    override val veto: (T) -> Boolean = { false },
    override val onChangedS: suspend IOnChangedScope2<P, T>.(T) -> Unit = {},
    override val onChanged: IOnChangedScope2<P, T>.(T) -> Unit = {}
) : DataClass(), IOnChangedParams2<P, T>

typealias OnChangedParams<T> = OnChangedParams2<Any?, T>