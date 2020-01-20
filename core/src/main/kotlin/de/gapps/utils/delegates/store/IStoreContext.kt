@file:Suppress("unused")

package de.gapps.utils.delegates.store

import java.io.File

@Suppress("FunctionName")
inline fun <reified T : Any> StoreContext(clazz: Class<T>) = StoreContext(clazz.name)

open class StoreContext(override val key: String) : IStoreContext

/**
 *
 */
interface IStoreContext {

    val key: String

    fun storeValue(
        propertyName: String,
        v: String
    ) = key(key, propertyName).contentEntity.write(v)

    fun readValue(
        propertyName: String
    ) = key(key, propertyName).contentEntity.read()

    fun key(contextKey: String, propertyKey: String) = "$contextKey$propertyKey"

    val String.contentEntity: IContentEntity
        get() = FileEntity(File(this))
}

interface IContentEntity {

    fun write(text: String)
    fun read(): String
}

class FileEntity(private val file: File) : IContentEntity {
    override fun write(text: String) = file.writeText(text)
    override fun read() = file.readText()
}