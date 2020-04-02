package dev.zieger.utils.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeExDeserializer
import dev.zieger.utils.time.duration.DurationExDeserializer
import dev.zieger.utils.time.duration.IDurationEx
import kotlin.reflect.KClass

open class JsonConverter(val config: SimpleModule.() -> Unit = {}) {

    val mapper: ObjectMapper = jacksonObjectMapper().registerModule(SimpleModule().apply {
        addDeserializer(ITimeEx::class.java, TimeExDeserializer())
        addDeserializer(IDurationEx::class.java, DurationExDeserializer())
        config()
    })

    fun <T : Any> T.toJson(): String? = catch(null) { mapper.writeValueAsString(this) }

    inline fun <reified T : Any> String.fromJson(): T? = catch(null) { mapper.readValue<T>(this) }
    fun <T : Any> String.fromJson(type: KClass<T>): T? = catch(null) { mapper.readValue<T>(this, type.java) }
    inline fun <reified T : Any> String.fromJsonList(): List<T>? = catch(null) { mapper.readValue<List<T>>(this) }

}
