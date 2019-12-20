package de.gapps.utils.delegates.store

import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.json.DeSerializer
import kotlin.reflect.KProperty

@Suppress("FunctionName")
inline fun <reified P : IStoreContext, reified T : Any> StoredProperty(
    initial: T,
    serializer: DeSerializer<T>,
    noinline onChange: T.(T) -> Unit
) = object : OnChanged<P, T>(initial, onChange) {

    override fun getValue(thisRef: P, property: KProperty<*>) =
        thisRef.readValue(property.name, T::class, serializer)!!

    override fun setValue(thisRef: P, property: KProperty<*>, value: T) =
        thisRef.storeValue(property.name, T::class, serializer, value)
}