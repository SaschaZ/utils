package de.gapps.utils.delegates

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


open class OnChanged<P : Any, T>(
    initial: T,
    private val onChange: T.(T) -> Unit
) : ReadWriteProperty<P, T> {

    private var valueFieldInternal: T = initial
    protected open var P.valueField: T
        get() = valueFieldInternal
        set(new) {
            if (new != valueFieldInternal) {
                val old = valueFieldInternal
                valueFieldInternal = new
                onPropertyChanged(new, old)
            }
        }

    protected open fun P.onPropertyChanged(new: T, old: T) = old.onChange(new)

    override fun setValue(thisRef: P, property: KProperty<*>, value: T) {
        thisRef.valueField = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = thisRef.valueField
}

