package dev.zieger.utils.json

import kotlin.reflect.KClass


interface ISerializer<out T : Any> {

    fun @UnsafeVariance T.serialize(): String?
    fun List<@UnsafeVariance T>.serialize(): String?
    fun String.deserialize(): T?
    fun String.deserializeList(): List<T>?
}

@Suppress("FunctionName")
inline fun <reified T : Any> DefaultSerializer(): ISerializer<T> = DefaultJsonConverter(T::class)

open class DefaultJsonConverter<out T : Any>(private val clazz: KClass<@UnsafeVariance T>) : JsonConverter(),
    ISerializer<T> {
    override fun @UnsafeVariance T.serialize(): String? = toJson(clazz.java)
    override fun List<@UnsafeVariance T>.serialize(): String? = toJson(clazz.java)
    override fun String.deserialize(): T? = fromJson(clazz.java)
    override fun String.deserializeList(): List<T>? = fromJsonList(clazz.java)
}