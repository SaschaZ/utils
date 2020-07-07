package dev.zieger.utils.json

import kotlin.reflect.KClass


interface ISerializer<in T : Any> {

    fun T.serialize(): String?
    fun List<T>.serialize(): String?
}

interface IDeserializer<out T : Any> {
    fun String.deserialize(): T?
    fun String.deserializeList(): List<T>?
}

interface IConverter<T : Any> : ISerializer<T>, IDeserializer<T>

@Suppress("FunctionName")
inline fun <reified T : Any> DefaultSerializer(): IConverter<T> = DefaultJsonConverter(T::class)

open class DefaultJsonConverter<T : Any>(private val clazz: KClass<T>) : JsonConverter(), IConverter<T> {
    override fun T.serialize(): String? = toJson(clazz)
    override fun List<T>.serialize(): String? = toJson(clazz)
    override fun String.deserialize(): T? = fromJson(clazz)
    override fun String.deserializeList(): List<T>? = fromJsonList(clazz)
}