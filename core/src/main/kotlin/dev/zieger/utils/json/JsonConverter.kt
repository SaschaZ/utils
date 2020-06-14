@file:Suppress("unused")

package dev.zieger.utils.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.zieger.utils.misc.catch
import java.lang.reflect.Type

interface IJsonConverterContext {

    val converter: JsonConverter
        get() = JsonConverter()

    fun <T> json(block: JsonConverter.() -> T): T = JsonConverter().block()
}

data class JsonConverterContext(override val converter: JsonConverter = JsonConverter()) : IJsonConverterContext

open class JsonConverter(vararg adapter: Any) {

    companion object {

        private val <T> T.CATCH_MESSAGE: (T) -> String get() = { "catch $it" }
    }

    private val moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).also { m -> adapter.forEach { m.add(it) } }.build()!!

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
