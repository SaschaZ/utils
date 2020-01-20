package de.gapps.utils.delegates

import de.gapps.utils.misc.asUnit
import de.gapps.utils.observable.ChangeObserver
import de.gapps.utils.observable.OnChangedScope
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


open class OnChanged<P : Any, out T>(
    initial: T,
    private val storeRecentValues: Boolean = false,
    notifyForExisting: Boolean = false,
    private val notifyOnChangedValueOnly: Boolean = true,
    private val onChange: ChangeObserver<T> = {}
) : ReadWriteProperty<P, @UnsafeVariance T> {
    private var valueFieldInternal: T = initial
    protected val recentValues = ArrayList<@UnsafeVariance T>()

    protected open var P.valueField: @UnsafeVariance T
        get() = valueFieldInternal
        set(new) {
            if (new != valueFieldInternal || !notifyOnChangedValueOnly) {
                val old = valueFieldInternal
                valueFieldInternal = new
                onPropertyChanged(new, old)
            }
        }

    init {
        if (notifyForExisting) {
            OnChangedScope(initial, null, emptyList()) { clearRecentValues() }.apply {
                onChange(initial)
            }
        }
    }

    fun clearRecentValues() = recentValues.clear().asUnit()

    protected open fun P.onPropertyChanged(new: @UnsafeVariance T, old: @UnsafeVariance T) {
        if (storeRecentValues) recentValues.add(old)
        OnChangedScope(new, old, recentValues) { clearRecentValues() }
    }

    override fun setValue(thisRef: P, property: KProperty<*>, value: @UnsafeVariance T) {
        thisRef.valueField = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = thisRef.valueField
}

