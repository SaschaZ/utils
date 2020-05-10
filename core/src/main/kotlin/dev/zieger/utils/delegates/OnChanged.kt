@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis", "unused")

package dev.zieger.utils.delegates

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Same as [IOnChanged2] but with constant parent of [Any]?.
 */
interface IOnChanged<out T : Any?> : IOnChangedBase<Any?, T, IOnChangedScope<T>>

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

interface IOnChangedBase<P : Any?, out T : Any?, out S : IOnChangedScope2<@UnsafeVariance P, T>> :
    IScope2Factory<P, T, @UnsafeVariance S>, ReadWriteProperty<P, @UnsafeVariance T> {
    val value: @UnsafeVariance T

    val storeRecentValues: Boolean
    val notifyForInitial: Boolean
    val notifyOnChangedValueOnly: Boolean
    val scope: CoroutineScope?
    val mutex: Mutex?

    /**
     * Is invoked before every change of the property. When returning `true` the new value is not assigned
     * to the property.
     */
    fun vetoInternal(value: @UnsafeVariance T): Boolean

    /**
     * Suspend on change callback. Only is invoked when [scope] is set.
     * The [IOnChangedScope2] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    suspend fun (@UnsafeVariance S).onChangedSInternal(value: @UnsafeVariance T)

    /**
     * Unsuspended on change callback. Will be called immediately when a new value is set.
     * The [IOnChangedScope2] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    fun (@UnsafeVariance S).onChangedInternal(value: @UnsafeVariance T)

    /**
     * Clears the recent value storage.
     */
    fun clearRecentValues()
}

interface IScope2Factory<P : Any?, out T : Any?, out S : IOnChangedScope2<P, T>> {
    fun createScope(
        value: @UnsafeVariance T,
        thisRef: P?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean = false,
        setter: (T) -> Unit = {}
    ): S
}

class OnChangedScope2Factory<P : Any?, out T : Any?> : IScope2Factory<P, T, IOnChangedScope2<P, @UnsafeVariance T>> {
    override fun createScope(
        value: @UnsafeVariance T,
        thisRef: P?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean,
        setter: (T) -> Unit
    ): IOnChangedScope2<P, @UnsafeVariance T> =
        OnChangedScope2(value, thisRef, previousValue, previousValues, clearPreviousValues, isInitialNotification)
}

interface IScopeFactory<out T : Any?, out S : IOnChangedScope<T>> : IScope2Factory<Any?, T, S>

class OnChangedScopeFactory<out T : Any?> : IScopeFactory<T, IOnChangedScope<@UnsafeVariance T>> {
    override fun createScope(
        value: @UnsafeVariance T,
        thisRef: Any?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean,
        setter: (T) -> Unit
    ): IOnChangedScope<@UnsafeVariance T> =
        OnChangedScope(value, thisRef, previousValue, previousValues, clearPreviousValues, isInitialNotification)
}

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
    initial, storeRecentValues, notifyForInitial, notifyOnChangedValueOnly, scope, mutex, OnChangedScope2Factory(),
    veto, onChangedS, onChanged
), IOnChanged2<P, T>

open class OnChangedBase<P : Any?, out T : Any?, out S : IOnChangedScope2<P, T>>(
    initial: T,
    override val storeRecentValues: Boolean,
    notifyForInitial: Boolean,
    override val notifyOnChangedValueOnly: Boolean,
    scope: CoroutineScope?,
    override val mutex: Mutex?,
    scopeFactory: IScope2Factory<P, T, S>,
    open val veto: (@UnsafeVariance T) -> Boolean,
    open val onChangedS: suspend (@UnsafeVariance S).(@UnsafeVariance T) -> Unit,
    open val onChanged: (@UnsafeVariance S).(@UnsafeVariance T) -> Unit
) : IOnChangedBase<P, T, S>, IScope2Factory<P, T, @UnsafeVariance S> by scopeFactory {

    @Suppress("CanBePrimaryConstructorProperty")
    override val notifyForInitial: Boolean = notifyForInitial

    @Suppress("CanBePrimaryConstructorProperty")
    override val scope: CoroutineScope? = scope

    protected var previousThisRef = AtomicReference<P?>(null)
    protected val recentValues = ArrayList<@UnsafeVariance T?>()

    override var value: @UnsafeVariance T = initial
        set(newValue) {
            val block = {
                if (!vetoInternal(newValue) && (field != newValue || !notifyOnChangedValueOnly)) {
                    val old = field
                    field = newValue
                    onPropertyChanged(value, old)
                }
            }
            scope?.launchEx(mutex = mutex) { block() } ?: block()
        }

    init {
        if (notifyForInitial) createScope(
            initial, previousThisRef.get(), null, recentValues,
            { clearRecentValues() }, true
        ).apply {
            onChanged(initial)
            scope?.launchEx(mutex = mutex) { onChangedS(initial) }
        }
    }

    override fun clearRecentValues() = recentValues.clear().asUnit()

    private fun onPropertyChanged(
        new: @UnsafeVariance T,
        old: @UnsafeVariance T?
    ) {
        if (storeRecentValues) recentValues.add(old)
        notifyListener(new, old)
    }

    private fun notifyListener(
        new: @UnsafeVariance T, old: @UnsafeVariance T?,
        isInitialNotification: Boolean = false
    ) = createScope(new, previousThisRef.get(), old, recentValues, { clearRecentValues() }, isInitialNotification)
        .apply {
            onChangedInternal(new)
            scope?.launchEx(mutex = mutex) { onChangedSInternal(new) }
        }

    override fun setValue(thisRef: P, property: KProperty<*>, value: @UnsafeVariance T) {
        previousThisRef.set(thisRef)
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = value

    override fun vetoInternal(value: @UnsafeVariance T): Boolean = veto(value)

    override suspend fun @UnsafeVariance S.onChangedSInternal(value: @UnsafeVariance T) =
        onChangedS(value)

    override fun @UnsafeVariance S.onChangedInternal(value: @UnsafeVariance T) = onChanged(value)
}

