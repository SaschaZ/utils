package de.gapps.utils.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.gapps.utils.misc.catch
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeExDeserializer
import de.gapps.utils.time.duration.DurationExDeserializer
import de.gapps.utils.time.duration.IDurationEx
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

}
