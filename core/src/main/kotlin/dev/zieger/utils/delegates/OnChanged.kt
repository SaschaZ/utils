@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis", "unused")

package dev.zieger.utils.delegates

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

/**
 * Same as [IOnChanged2] but with constant parent of [Any]?.
 */
interface IOnChanged<out T : Any?> : IOnChangedBase<Any?, T, IOnChangedScope<T>>

/**
 * Same as [OnChanged2] but with constant parent of [Any]?.
 */
open class OnChanged<out T : Any?>(
    initial: T,
    override val storeRecentValues: Boolean = false,
    override val notifyForInitial: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val scope: CoroutineScope? = null,
    override val mutex: Mutex? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onChangedS: suspend IOnChangedScope<@UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {},
    onChanged: IOnChangedScope<@UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}
) : OnChangedBase<Any?, @UnsafeVariance T, IOnChangedScope<T>>(
    initial, storeRecentValues, notifyForInitial, notifyOnChangedValueOnly, scope, mutex, OnChangedScopeFactory(),
    veto, onChangedS, onChanged
), IOnChanged<T>

