@file:Suppress("MemberVisibilityCanBePrivate")

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
interface IOnChanged<T> : IOnChanged2<Any?, T>

/**
 * [ReadWriteProperty] with support for listener that get called within a [IOnChangedScope] (suspend und unsuspended).
 *
 * @property value property that will notify listener when it changes
 * @property storeRecentValues If set to `true` all values of the property are stored and provided within the
 * [IOnChangedScope]. Should be set to `false` when the values of the property consume too much memory.
 * @property notifyForExisting When `true` a new listener will immediately notified for the existing value of the
 * property without the need of a change.
 * @property notifyOnChangedValueOnly When `false` listener are only notified when the value of the property changed.
 * When `true` every "set" to the property will notify the listener.
 * @property scope [CoroutineScope] that is used to notify listener that requested to be notified within a coroutine.
 * @property mutex If not `null` the [Mutex] will wrap the whole execution of [scope].
 * @property veto Is invoked before every change of the property. When returning `true` the new value is not assigned
 * to the property.
 * @property onChangedS Suspend on change callback. Only is invoked when [scope] is set.
 * @property onChange Unsuspended on change callback.
 */
interface IOnChanged2<P : Any?, out T : Any?> : ReadWriteProperty<P, @kotlin.UnsafeVariance T> {
    var value: @UnsafeVariance T

    val storeRecentValues: Boolean
    val notifyForExisting: Boolean
    val notifyOnChangedValueOnly: Boolean
    val scope: CoroutineScope?
    val mutex: Mutex?

    val veto: (@UnsafeVariance T) -> Boolean
    val onChangedS: suspend IOnChangedScope<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit
    var onChange: IOnChangedScope<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit

    /**
     * Clears the recent value storage.
     */
    fun clearRecentValues()
}

/**
 * Same as [OnChanged2] but with constant parent of [Any]?.
 */
typealias OnChanged<T> = OnChanged2<Any?, T>

/**
 * Simple implementation of [IOnChanged2].
 */
class OnChanged2<P : Any?, out T : Any?>(
    initial: T,
    override val storeRecentValues: Boolean = false,
    override val notifyForExisting: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val scope: CoroutineScope? = null,
    override val mutex: Mutex? = null,
    override val veto: (@UnsafeVariance T) -> Boolean = { false },
    override val onChangedS: suspend IOnChangedScope<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {},
    override var onChange: IOnChangedScope<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}

) : IOnChanged2<P, @UnsafeVariance T> {

    private var triggeredChangeRunning = false

    private var previousThisRef = AtomicReference<P?>(null)
    private val recentValues = ArrayList<@UnsafeVariance T?>()

    override var value: @UnsafeVariance T = initial
        set(newValue) {
            val block = {
                if (!veto(newValue) && (field != newValue || !notifyOnChangedValueOnly || triggeredChangeRunning)) {
                    triggeredChangeRunning = false
                    val old = field
                    field = newValue
                    onPropertyChanged(previousThisRef.get(), value, old, recentValues)
                }
            }
            scope?.launchEx(mutex = mutex) { block() } ?: block()
        }

    init {
        if (notifyForExisting) triggerOnChanged()
    }

    override fun clearRecentValues() = recentValues.clear().asUnit()

    private fun onPropertyChanged(
        thisRef: P?,
        new: @UnsafeVariance T,
        old: @UnsafeVariance T?,
        previous: List<@UnsafeVariance T?>
    ) {
        if (storeRecentValues) recentValues.add(old)
        notifyListener(thisRef, new, old, previous)
    }

    private fun triggerOnChanged() {
        triggeredChangeRunning = true
        value = value
    }

    private fun notifyListener(
        thisRef: P?, new: @UnsafeVariance T, old: @UnsafeVariance T?,
        previous: List<@UnsafeVariance T?>
    ) = OnChangedScope(new, thisRef, old, previous) { clearRecentValues() }.apply {
        scope?.launchEx(mutex = mutex) { onChangedS(new) }
        onChange(new)
    }

    override fun setValue(thisRef: P, property: KProperty<*>, value: @UnsafeVariance T) {
        previousThisRef.set(thisRef)
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = value
}

