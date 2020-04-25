package dev.zieger.utils.json

import kotlinx.serialization.KSerializer

interface ISerializer<out T : Any> {

    fun @UnsafeVariance T.serialize(): String?
    fun List<@UnsafeVariance T>.serialize(): String?
    fun String.deserialize(): T?
    fun String.deserializeList(): List<T>?
}

@Suppress("FunctionName")
inline fun <reified T : Any> DefaultSerializer(serializer: KSerializer<T>): ISerializer<T> =
    object : JsonConverter(), ISerializer<T> {
        override fun T.serialize(): String? = toJson(serializer)
        override fun List<T>.serialize(): String? = toJson(serializer)
        override fun String.deserialize(): T? = fromJson(serializer)
        override fun String.deserializeList(): List<T>? = fromJsonList(serializer)
    }