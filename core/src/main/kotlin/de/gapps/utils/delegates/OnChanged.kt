@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.delegates

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.misc.asUnit
import de.gapps.utils.observable.IOnChangedScope
import de.gapps.utils.observable.OnChangedScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface IOnChanged<P : Any?, out T : Any?> : ReadWriteProperty<P, @kotlin.UnsafeVariance T> {
    var value: @UnsafeVariance T

    val storeRecentValues: Boolean
    val notifyForExisting: Boolean
    val notifyOnChangedValueOnly: Boolean
    val scope: CoroutineScope?
    val mutex: Mutex?

    fun clearRecentValues()
}

class OnChanged<P : Any?, out T : Any?>(
    initial: T,
    override val storeRecentValues: Boolean = false,
    override val notifyForExisting: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val scope: CoroutineScope? = null,
    override val mutex: Mutex? = null,
    private val onChangedS: suspend IOnChangedScope<P, T>.(T) -> Unit = {},
    internal var onChange: IOnChangedScope<P, @UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}
) : IOnChanged<P, @UnsafeVariance T> {

    private var triggeredChangeRunning = false

    private var previousThisRef = AtomicReference<P?>(null)
    private val recentValues = ArrayList<@UnsafeVariance T?>()

    override var value: @UnsafeVariance T = initial
        set(newValue) {
            val block = {
                if (field != newValue || !notifyOnChangedValueOnly || triggeredChangeRunning) {
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
    ) =
        OnChangedScope(new, thisRef, old, previous) { clearRecentValues() }.apply {
            scope?.launchEx(mutex = mutex) { onChangedS(new) }
            onChange(new)
        }

    override fun setValue(thisRef: P, property: KProperty<*>, value: @UnsafeVariance T) {
        previousThisRef.set(thisRef)
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = value
}

