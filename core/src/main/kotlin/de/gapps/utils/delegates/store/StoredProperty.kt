@file:Suppress("unused")

package de.gapps.utils.delegates.store

import de.gapps.utils.delegates.IOnChangedScope
import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.json.DefaultSerializer
import de.gapps.utils.json.ISerializer
import de.gapps.utils.misc.asUnit
import kotlinx.serialization.KSerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Uses the provided [serializer] to write and read the property from the holding [IStoreContext] ([P])
 */
@Suppress("FunctionName")
inline fun <reified P : IStoreContext, reified T : Any> StoredProperty(
    initial: T,
    kSerializer: KSerializer<T>,
    serializer: ISerializer<T> = DefaultSerializer(kSerializer),
    noinline onChange: IOnChangedScope<*, T>.(T) -> Unit = {}
) = object : ReadWriteProperty<P, T> {

    override fun getValue(thisRef: P, property: KProperty<*>) =
        serializer.run { thisRef.readValue(property.name).deserializeList()?.first() ?: initial }

    private var internalValue: T by OnChanged(initial, onChange = onChange)

    override fun setValue(thisRef: P, property: KProperty<*>, value: T) {
        serializer.run { value.serialize()?.let { thisRef.storeValue(property.name, it) }.asUnit() }
    }
}