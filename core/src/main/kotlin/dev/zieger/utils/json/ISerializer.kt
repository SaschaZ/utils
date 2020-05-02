package dev.zieger.utils.json


interface ISerializer<out T : Any> {

    fun @UnsafeVariance T.serialize(): String?
    fun List<@UnsafeVariance T>.serialize(): String?
    fun String.deserialize(): T?
    fun String.deserializeList(): List<T>?
}

@Suppress("FunctionName")
inline fun <reified T : Any> DefaultSerializer(): ISerializer<T> =
    object : JsonConverter(), ISerializer<T> {
        override fun T.serialize(): String? = toJson()
        override fun List<T>.serialize(): String? = toJson()
        override fun String.deserialize(): T? = fromJson()
        override fun String.deserializeList(): List<T>? = fromJsonList()
    }