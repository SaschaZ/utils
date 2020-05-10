package dev.zieger.utils.delegates

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.properties.ReadWriteProperty


/**
 * [ReadWriteProperty] with support for a listener that is called when the observed property changes.
 *
 * @property value property that will notify listener when it changes
 * @property storeRecentValues If set to `true` all values of the property are stored and provided within the
 * [IOnChangedScope2]. Should be set to `false` when the values of the property consume too much memory.
 * @property notifyForInitial When `true` a new listener will immediately notified for the existing value of the
 * property without the need of a change.
 * @property notifyOnChangedValueOnly When `false` listener are only notified when the value of the property changed.
 * When `true` every "set" to the property will notify the listener.
 * @property scope [CoroutineScope] that is used to notify listener that requested to be notified within a coroutine.
 * @property mutex If not `null` the [Mutex] will wrap the whole execution of [scope].
 */
interface IOnChanged2<P : Any?, out T : Any?> : IOnChangedBase<P, T, IOnChangedScope2<P, T>>

/**
 * Simple implementation of [IOnChanged2].
 *
 * @param initial The observed property will be initialized with this value.
 * @param storeRecentValues If set to `true` all values of the property will be stored and provided within the
 * [IOnChangedScope2]. Should be set to `false` when the values of the property consume too much memory.
 * Defaulting is `false`.
 * @param notifyForInitial When `true` a new listener will immediately notified for the initial value of the
 * property without the need of a change. Default is `false`.
 * @param notifyOnChangedValueOnly When `false` listener are only notified when the value of the property changed.
 * When `true` every "set" to the property will notify the listener. Default is `true`.
 * @param scope [CoroutineScope] that is used to notify listener that requested to be notified within a coroutine.
 * Default is `null`.
 * @param mutex If not `null` the [Mutex] will wrap the whole execution of [scope]. Default is `null`.
 * @param veto Is invoked before every change of the property. When returning `true` the new value is not assigned
 * to the property. (Optional)
 * @param onChangedS Suspend on change callback. Only is invoked when [scope] is set. (Optional)
 * @param onChanged Unsuspended on change callback. Will be called immediately when a new value is set. (Optional)
 */
open class OnChanged2<P : Any?, out T : Any?>(
    initial: T,
    storeRecentValues: Boolean = false,
    notifyForInitial: Boolean = false,
    notifyOnChangedValueOnly: Boolean = true,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onChangedS: suspend IOnChangedScope2<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {},
    onChanged: IOnChangedScope2<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}
) : OnChangedBase<P, T, IOnChangedScope2<P, T>>(
    initial, storeRecentValues, notifyForInitial, notifyOnChangedValueOnly, scope, mutex,
    OnChangedScope2Factory(),
    veto, onChangedS, onChanged
), IOnChanged2<P, T>