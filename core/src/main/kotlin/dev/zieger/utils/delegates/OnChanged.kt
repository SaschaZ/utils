@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis", "unused", "FunctionName")

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
    ReadWriteProperty<P, @UnsafeVariance T>, IScopeFactory<P, T, S> {

    val value: T
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
     * Unsuspended on change callback. Will be called immediately when a new value is set.
     * The [IOnChangedScope2] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    fun (@UnsafeVariance S).onChangedInternal(value: @UnsafeVariance T)

    /**
     * Clears the recent value storage.
     */
    fun clearRecentValues()

    var previousThisRef: AtomicReference<P?>
}

interface IOnChangedWritableBase<P : Any?, out T : Any?, out S : IOnChangedScope2<@UnsafeVariance P, T>> :
    IOnChangedBase<P, T, S> {
    override var value: @UnsafeVariance T
}

interface IScopeFactory<P : Any?, out T : Any?, out S : IOnChangedScope2<P, T>> {

    fun createScope(
        newValue: @UnsafeVariance T, thisRef: P?, previousValue: @UnsafeVariance T? = null,
        recentValues: List<@UnsafeVariance T?> = emptyList(), clearRecentValues: () -> Unit = {},
        isInitialNotification: Boolean = false, setter: (T) -> Unit = {}
    ): S
}

class OnChangedScopeFactory<out T : Any?> : IScopeFactory<Any?, T, IOnChangedScope<T>> {

    override fun createScope(
        newValue: @UnsafeVariance T, thisRef: Any?, previousValue: @UnsafeVariance T?,
        recentValues: List<@UnsafeVariance T?>, clearRecentValues: () -> Unit,
        isInitialNotification: Boolean, setter: (T) -> Unit
    ): IOnChangedScope<T> =
        OnChangedScope(newValue, thisRef, previousValue, recentValues, clearRecentValues, isInitialNotification)
}

class OnChangedScope2Factory<P : Any?, out T : Any?> : IScopeFactory<P, T, IOnChangedScope2<P, T>> {

    override fun createScope(
        newValue: @UnsafeVariance T, thisRef: P?, previousValue: @UnsafeVariance T?,
        recentValues: List<@UnsafeVariance T?>, clearRecentValues: () -> Unit,
        isInitialNotification: Boolean, setter: (T) -> Unit
    ): IOnChangedScope2<P, T> =
        OnChangedScope2(newValue, thisRef, previousValue, recentValues, clearRecentValues, isInitialNotification)
}

/**
 * Same as [OnChanged2] but with constant parent of [Any]?.
 */
inline fun <T : Any?> OnChanged(
    initial: T,
    storeRecentValues: Boolean = false,
    notifyForInitial: Boolean = false,
    notifyOnChangedValueOnly: Boolean = true,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    crossinline veto: (@UnsafeVariance T) -> Boolean = { false },
    crossinline onChanged: IOnChangedScope<@UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}
): IOnChanged<T> = object : IOnChanged<T>, IOnChangedBase<Any?, T, IOnChangedScope<T>> by OnChangedBase(
    initial, storeRecentValues, notifyForInitial, notifyOnChangedValueOnly, scope, mutex, OnChangedScopeFactory(),
    veto, onChanged
) {}

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
 * @param onChanged Unsuspended on change callback. Will be called immediately when a new value is set. (Optional)
 */
inline fun <P : Any?, T : Any?> OnChanged2(
    initial: T,
    storeRecentValues: Boolean = false,
    notifyForInitial: Boolean = false,
    notifyOnChangedValueOnly: Boolean = true,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    crossinline veto: (@UnsafeVariance T) -> Boolean = { false },
    crossinline onChanged: IOnChangedScope2<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}
): IOnChanged2<P, T> = object : IOnChanged2<P, T>, IOnChangedBase<P, T, IOnChangedScope2<P, T>> by OnChangedBase(
    initial, storeRecentValues, notifyForInitial, notifyOnChangedValueOnly, scope, mutex, OnChangedScope2Factory(),
    veto, onChanged
) {}

inline fun <P : Any?, T : Any?, S : IOnChangedScope2<P, T>> OnChangedBase(
    initial: T,
    storeRecentValues: Boolean = false,
    notifyForInitial: Boolean = false,
    notifyOnChangedValueOnly: Boolean = true,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    scopeFactory: IScopeFactory<P, T, S>,
    crossinline veto: (@UnsafeVariance T) -> Boolean,
    crossinline onChanged: (@UnsafeVariance S).(@UnsafeVariance T) -> Unit
): IOnChangedWritableBase<P, T, S> = object : IOnChangedWritableBase<P, T, S>,
    IScopeFactory<P, T, S> by scopeFactory {

    override val storeRecentValues: Boolean = storeRecentValues
    override val notifyForInitial: Boolean = notifyForInitial
    override val notifyOnChangedValueOnly: Boolean = notifyOnChangedValueOnly
    override val scope: CoroutineScope? = scope
    override val mutex: Mutex? = mutex

    override var previousThisRef = AtomicReference<P?>(null)
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
        if (notifyForInitial)
            notifyListener(initial, null, true)
    }

    override fun clearRecentValues() = recentValues.clear().asUnit()

    private fun onPropertyChanged(
        new: T,
        old: T?,
        isInitial: Boolean = false
    ) {
        if (storeRecentValues && !isInitial) recentValues.add(old)
        notifyListener(new, old)
    }

    private fun notifyListener(
        new: T,
        old: T?,
        isInitialNotification: Boolean = false
    ) = createScope(
        new, previousThisRef.get(), old, recentValues, { recentValues.clear() }, isInitialNotification
    ).onChangedInternal(new)

    override fun setValue(thisRef: P, property: KProperty<*>, value: T) {
        previousThisRef.set(thisRef)
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = value

    override fun vetoInternal(value: T): Boolean = veto(value)

    override fun S.onChangedInternal(value: T) = onChanged(value)
}

