package de.gapps.utils.json

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.gapps.utils.misc.asUnit
import de.gapps.utils.misc.catch

open class JsonConverter(vararg adapter: JsonAdapter<*>) {

    val moshi: Moshi = Moshi.Builder().apply {
        for (jsonAdapter in adapter) add(jsonAdapter)
        add(object : JsonAdapter<Pair<String, String>>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Pair<String, String>? = reader.run {
                beginObject()
                nextString() // first
                val first = nextString()
                nextString() // second
                val second = nextString()
                endObject()
                first to second
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Pair<String, String>?) = writer.run {
                value?.let { pair ->
                    beginObject()
                    buildString { append("first") }
                    buildString { append(pair.first) }
                    buildString { append("second") }
                    buildString { append(pair.second) }
                    endObject()
                }
            }.asUnit()
        })
        add(KotlinJsonAdapterFactory())
    }.build()

    inline fun <reified T : Any> T.toJson(): String? =
        catch(null, onCatch = { print(it) }) { moshi.adapter(T::class.java).toJson(this) }

    inline fun <reified T : Any, reified L : List<T>> L.toJson(): String? =
        catch(null, onCatch = { print(it) }) { moshi.adapter(L::class.java).toJson(this) }

    inline fun <reified T : Any> String.fromJson(): T? =
        catch(null, onCatch = { print(it) }) { moshi.adapter(T::class.java).fromJson(this) }

    inline fun <reified T : Any, reified L : List<T>> String.fromJsonList(): List<T>? =
        catch(null, onCatch = { print(it) }) { moshi.adapter(L::class.java).fromJson(this) }
}
