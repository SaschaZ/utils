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
interface IOnChanged2<P : Any?, T : Any?> : IOnChangedBase<P, T>

typealias IOnChanged<T> = IOnChanged2<Any?, T>

/**
 * Simple implementation of [IOnChanged2].
 */
open class OnChanged2<P : Any?, T : Any?>(
    params: IOnChangedParams2<P, T>
) : OnChangedBase<P, T>(params), IOnChanged2<P, T> {
    constructor(
        initial: T,
        onChanged: IOnChangedScope2<P, T>.(T) -> Unit = {}
    ) :
            this(OnChangedParams2(initial, scopeFactory = OnChangedScope2Factory(), onChanged = onChanged))
}

typealias OnChanged<T> = OnChanged2<Any?, T>