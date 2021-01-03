@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.json

import com.squareup.moshi.*
import com.squareup.moshi.JsonAdapter
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.string.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import okio.BufferedSink
import okio.BufferedSource
import java.io.OutputStream
import java.lang.reflect.Type
import kotlin.reflect.KClass

abstract class JsonAdapter<T> {

    @ToJson
    abstract fun toJson(value: T): String

    @FromJson
    abstract fun fromJson(json: String): T
}

open class JsonConverter(vararg adapter: Any) {

    companion object {

        val <T> T.CATCH_MESSAGE: (T) -> String get() = { "catch $it" }
    }

    val moshi = Moshi.Builder()
        .also { m -> adapter.forEach { m.add(it) } }
        .add(TimeExJsonAdapter())
        .add(TimeZoneAdapter())
        .add(DurationExJsonAdapter())
        .add(ClosedTimeRangeJsonAdapter())
        .add(ClosedDoubleRangeJsonAdapter())
        .build()!!

    fun Any.toJson(
        rawType: KClass<*> = this::class,
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): String? = toJsonReified(rawType, *genericTypes, annotations = annotations, printException = printException)

    fun Any.toJson(
        sink: BufferedSink,
        rawType: KClass<*> = this::class,
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ) = toJsonReified(sink, rawType, *genericTypes, annotations = annotations, printException = printException)

    inline fun <reified T : Any> T.toJsonReified(
        rawType: KClass<*> = T::class,
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): String? = catch(null, printStackTrace = printException, logStackTrace = false) {
        moshi.adapter<T>(rawType.with(*genericTypes), *annotations).toJson(this)
    }

    inline fun <reified T : Any> T.toJsonReified(
        sink: BufferedSink,
        rawType: KClass<*> = T::class,
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ) = catch(Unit, printStackTrace = printException, logStackTrace = false) {
        moshi.adapter<T>(rawType.with(*genericTypes), *annotations).toJson(sink, this)
    }

    inline fun <reified T : Any> List<T>.toJson(
        type: Type = T::class.java,
        printException: Boolean = true
    ): String? = toJsonReified(List::class, type, printException = printException)

    inline fun <reified K : Any, reified V : Any> Map<K, V>.toJson(
        keyType: KClass<*> = K::class,
        valueType: KClass<*> = V::class,
        printException: Boolean = true
    ): String? = toJsonReified(Map::class, keyType.java, valueType.java, printException = printException)

    suspend inline fun <reified T> Flow<T>.toJson(
        output: OutputStream,
        crossinline writer: suspend com.google.gson.stream.JsonWriter.(T) -> Unit
    ) {
        com.google.gson.stream.JsonWriter(output.bufferedWriter()).run {
            isLenient = true
            beginArray()
            collect { writer(it) }
            endArray()
        }
    }

    inline fun <reified T : Any> String.fromJson(
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): T? = fromJson(T::class, *genericTypes, annotations = annotations, printException = printException)

    fun <T : Any> String.fromJson(
        rawType: KClass<T>,
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): T? = catch(null, onCatch = { if (printException) print("${it.CATCH_MESSAGE} from '$this' ") }) {
        moshi.adapter<T>(rawType.with(*genericTypes), *annotations).fromJson(this)
    }

    inline fun <reified T : Any> BufferedSource.fromJson(
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): T? = fromJson(T::class, *genericTypes, annotations = annotations, printException = printException)

    fun <T : Any> BufferedSource.fromJson(
        rawType: KClass<T>,
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): T? = catch(null, onCatch = { if (printException) print("${it.CATCH_MESSAGE} from '$this' ") }) {
        moshi.adapter<T>(rawType.with(*genericTypes), *annotations).fromJson(this)
    }

    fun String.fromJsonNonTyped(
        rawType: KClass<*>,
        vararg genericTypes: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): Any? = catch(null, onCatch = { if (printException) print("${it.CATCH_MESSAGE} from '$this' ") }) {
        val type = if (genericTypes.isEmpty()) rawType.java else Types.newParameterizedType(rawType.java, *genericTypes)
        moshi.adapter<Any>(type, *annotations).fromJson(this)
    }

    inline fun <reified T : Any> String.fromJsonList(
        type: Type = T::class.java,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): List<T>? = fromJsonListNonDef(type, annotations, printException)

    inline fun <reified T : Any> BufferedSource.fromJsonList(
        type: Type = T::class.java,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): List<T>? = fromJsonListNonDef(type, annotations, printException)

    fun <T : Any> String.fromJsonListNonDef(
        type: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): List<T>? = catch(null, onCatch = { if (printException) print("${it.CATCH_MESSAGE} from '$this' ") }) {
        adapter<List<T>>(type, *annotations).fromJson(this)
    }

    fun <T : Any> BufferedSource.fromJsonListNonDef(
        type: Type,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): List<T>? = catch(null, onCatch = { if (printException) print("${it.CATCH_MESSAGE} from '$this' ") }) {
        adapter<List<T>>(type, *annotations).fromJson(this)
    }

    inline fun <reified K : Any, reified V : Any> String.fromJsonMap(
        keyType: KClass<*> = K::class,
        valueType: KClass<*> = V::class,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): Map<K, V>? = fromJsonMapNonDef(keyType, valueType, annotations, printException)

    fun <K : Any, V : Any> String.fromJsonMapNonDef(
        keyType: KClass<*>,
        valueType: KClass<*>,
        annotations: Array<Class<Annotation>> = emptyArray(),
        printException: Boolean = true
    ): Map<K, V>? =
        catch(null, onCatch = { if (printException) print("${it.CATCH_MESSAGE} from '$this' ") }) {
            adapter<Map<K, V>>(keyType.java, valueType.java, *annotations).fromJson(this)
        }

    inline fun <reified T : Any> adapter(vararg types: Type): JsonAdapter<T> =
        moshi.adapter(T::class.with(*types))
}

object UtilsJsonConverter : JsonConverter() {

    inline fun <reified T : Any> T.toJson(): String? = toJsonReified(T::class)
    inline fun <reified T : Any> List<T>.toJson(): String? = toJsonReified(List::class, T::class.java)

    inline fun <reified T : Any> T.toJson(sink: BufferedSink) = toJsonReified(sink, T::class)
    inline fun <reified T : Any> List<T>.toJson(sink: BufferedSink) = toJsonReified(sink, List::class, T::class.java)

    inline fun <reified T : Any> String.fromJson(): T? = fromJson(T::class)
    inline fun <reified T : Any> String.fromJsonList(): List<T>? = fromJsonList(T::class.java)

    inline fun <reified T : Any> BufferedSource.fromJson(): T? = fromJson(T::class)
    inline fun <reified T : Any> BufferedSource.fromJsonList(): List<T>? = fromJsonList(T::class.java)
}

inline fun <T> json(crossinline block: UtilsJsonConverter.() -> T): T = UtilsJsonConverter.run { block() }

fun KClass<*>.with(vararg types: KClass<*>, owner: KClass<*>? = null): Type =
    with(*types.map { it.java }.toTypedArray(), owner = owner?.java)

fun KClass<*>.with(vararg types: Type, owner: Type? = null): Type = when {
    types.isEmpty() -> java
    owner != null -> Types.newParameterizedTypeWithOwner(owner, java, *types.toList().toTypedArray())
    else -> Types.newParameterizedType(java, *types.toList().toTypedArray())
}

