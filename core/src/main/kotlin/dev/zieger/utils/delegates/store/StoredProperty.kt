@file:Suppress("unused")

package dev.zieger.utils.delegates.store

import dev.zieger.utils.delegates.IOnChangedScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.json.DefaultSerializer
import dev.zieger.utils.json.ISerializer
import dev.zieger.utils.misc.asUnit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Uses the provided [serializer] to write and read the property from the holding [IStoreContext] ([P])
 */
@Suppress("FunctionName")
inline fun <reified P : IStoreContext, reified T : Any> StoredProperty(
    initial: T,
    serializer: ISerializer<T> = DefaultSerializer(),
    key: String? = null,
    noinline onChange: IOnChangedScope<*, T>.(T) -> Unit = {}
) = object : ReadWriteProperty<P, T> {

    private var internalValue: T by OnChanged(initial, onChange = onChange)

    override fun getValue(thisRef: P, property: KProperty<*>) =
        serializer.run { thisRef.readValue(key ?: property.name).deserialize() ?: initial }

    override fun setValue(thisRef: P, property: KProperty<*>, value: T) =
        serializer.run { value.serialize()?.let { thisRef.storeValue(key ?: property.name, it) }.asUnit() }
}