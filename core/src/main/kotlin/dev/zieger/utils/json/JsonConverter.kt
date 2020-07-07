@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.json

import com.squareup.moshi.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.ClosedTimeRangeJsonAdapter
import dev.zieger.utils.time.DurationExJsonAdapter
import dev.zieger.utils.time.TimeExJsonAdapter
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

        private val <T> T.CATCH_MESSAGE: (T) -> String get() = { "catch $it" }
    }

    protected val moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(TimeExJsonAdapter())
            .add(DurationExJsonAdapter())
            .add(ClosedTimeRangeJsonAdapter())
            .also { m -> adapter.forEach { m.add(it) } }.build()!!

    fun <T : Any> T.toJson(type: KClass<*>): String? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            moshi.adapter<T>(type.java).toJson(this)
        }

    fun <T : Any> List<T>.toJson(type: KClass<*>): String? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            adapter<List<T>>(type).toJson(this)
        }

    fun <K : Any, V : Any> Map<K, V>.toJson(keyType: KClass<*>, valueType: KClass<*>): String? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            adapter<Map<K, V>>().toJson(this)
        }

    fun <T : Any> String.fromJson(type: KClass<*>): T? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            moshi.adapter<T>(type.java).fromJson(this)
        }

    fun <T : Any> String.fromJsonList(type: KClass<*>): List<T>? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            adapter<List<T>>(type).fromJson(this)
        }

    fun <K : Any, V : Any> String.fromJsonMap(keyType: KClass<*>, valueType: KClass<*>): Map<K, V>? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            adapter<Map<K, V>>(keyType, valueType).fromJson(this)
        }

    private fun KClass<*>.with(vararg types: KClass<*>): Type =
        Types.newParameterizedType(java, *types.map { it.java }.toTypedArray())

    private inline fun <reified T : Any> adapter(vararg types: KClass<*>): JsonAdapter<T> =
        moshi.adapter<T>(T::class.with(*types))
}

object UtilsJsonConverter : JsonConverter() {

    inline fun <reified T : Any> T.toJson(): String? = toJson(T::class)
    inline fun <reified T : Any> List<T>.toJson(): String? = toJson(T::class)

    inline fun <reified T : Any> String.fromJson(): T? = fromJson(T::class)
    inline fun <reified T : Any> String.fromJsonList(): List<T>? = fromJsonList(T::class)
}

