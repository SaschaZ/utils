@file:Suppress("unused")

package de.gapps.utils.delegates.store

import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.json.DeSerializer
import de.gapps.utils.json.JsonConverter
import de.gapps.utils.misc.asUnit
import de.gapps.utils.observable.ChangeObserver
import kotlin.reflect.KProperty

/**
 * Uses the provided [serializer] to write and read the property from the holding [IStoreContext] ([P])
 */
@Suppress("FunctionName")
inline fun <reified P : IStoreContext, reified T : Any> StoredProperty(
    initial: T,
    serializer: DeSerializer<T> = object : JsonConverter(), DeSerializer<T> {
        override fun serialize(value: T) = value.toJson()
        override fun serialize(value: List<T>) = value.toJson()
        override fun deserialize(value: String) = value.fromJsonList<T>()
    },
    noinline onChange: ChangeObserver<T> = {}
) = object : OnChanged<P, T>(initial, onChange = onChange) {

    override fun getValue(thisRef: P, property: KProperty<*>) =
        serializer.deserialize(thisRef.readValue(property.name))?.first() ?: initial

    override fun setValue(thisRef: P, property: KProperty<*>, value: T) =
        serializer.serialize(value)?.let { thisRef.storeValue(property.name, it) }.asUnit()
}