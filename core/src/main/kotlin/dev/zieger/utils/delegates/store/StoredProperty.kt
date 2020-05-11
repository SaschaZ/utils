@file:Suppress("unused")

package dev.zieger.utils.delegates.store

import dev.zieger.utils.delegates.IOnChangedScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.json.DefaultJsonConverter
import dev.zieger.utils.json.DefaultSerializer
import dev.zieger.utils.json.ISerializer
import dev.zieger.utils.misc.asUnit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Uses the provided [serializer] to write and read the property from the holding [IStoreContext] ([P])
 */
@Suppress("FunctionName")
inline fun <reified T : Any> StoredProperty(
    initial: T,
    serializer: ISerializer<T> = DefaultSerializer(),
    key: String? = null,
    noinline onChange: IOnChangedScope<@UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}
) = StoredProperty<T>(initial, T::class, serializer, key, onChange)

open class StoredProperty<out T : Any>(
    private val initial: T,
    private val clazz: KClass<T>,
    private val serializer: ISerializer<T> = DefaultJsonConverter(clazz),
    private val key: String? = null,
    private val onChanged: IOnChangedScope<@UnsafeVariance T>.(@UnsafeVariance T) -> Unit = {}
) : ReadWriteProperty<IStoreContext, @UnsafeVariance T> {

    private var internalValue: T by OnChanged(initial, onChanged = onChanged)

    override fun getValue(thisRef: IStoreContext, property: KProperty<*>) =
        serializer.run { thisRef.readValue(key ?: property.name).deserialize() ?: initial }

    override fun setValue(thisRef: IStoreContext, property: KProperty<*>, value: @UnsafeVariance T) =
        serializer.run { value.serialize()?.let { thisRef.storeValue(key ?: property.name, it) }.asUnit() }
}