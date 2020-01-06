package de.gapps.utils.delegates.store

import de.gapps.utils.json.DeSerializer
import de.gapps.utils.misc.asUnit
import java.io.File
import kotlin.reflect.KClass

open class StoreContext(override val key: String) : IStoreContext

interface IStoreContext {

    val key: String

    fun <T : Any> storeValue(
        propertyName: String,
        type: KClass<T>,
        serializer: DeSerializer<T>,
        v: T
    ) =
        serializer.serialize((v))?.let { key(key, propertyName).file.writeText(it) }.asUnit()

    fun <T : Any> readValue(
        propertyName: String,
        type: KClass<T>,
        serializer: DeSerializer<T>
    ) = serializer.deserialize(key(key, propertyName).file.readText())?.first()

    private fun key(contextKey: String, propertyKey: String) = "$contextKey$propertyKey"

    private val String.file: File
        get() = File(this)
}