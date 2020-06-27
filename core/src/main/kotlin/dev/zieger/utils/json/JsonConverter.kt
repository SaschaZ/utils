@file:Suppress("unused")

package dev.zieger.utils.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.DurationExJsonAdapter
import dev.zieger.utils.time.TimeExJsonAdapter
import java.lang.reflect.Type

interface IJsonConverterContext {

    val converter: JsonConverter get() = JsonConverter()

    fun <T> json(block: JsonConverter.() -> T): T = converter.block()
}

data class JsonConverterContext(override val converter: JsonConverter = JsonConverter()) : IJsonConverterContext

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

    private val moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(TimeExJsonAdapter())
            .add(DurationExJsonAdapter())
            .also { m -> adapter.forEach { m.add(it) } }.build()!!

    fun <T : Any> T.toJson(type: Type): String? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            moshi.adapter<T>(type).toJson(this)
        }

    fun <T : Any> List<T>.toJson(type: Type): String? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            val typeToUse: Type = Types.newParameterizedType(
                List::class.java, type
            )
            moshi.adapter<List<T>>(typeToUse).toJson(this)
        }

    fun <T : Any> String.fromJson(type: Type): T? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            moshi.adapter<T>(type).fromJson(this)
        }

    fun <T : Any> String.fromJsonList(type: Type): List<T>? =
        catch(null, onCatch = { print(it.CATCH_MESSAGE) }) {
            val typeToUse: Type = Types.newParameterizedType(
                List::class.java, type
            )
            moshi.adapter<List<T>>(typeToUse).fromJson(this)
        }
}
