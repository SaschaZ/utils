package dev.zieger.utils.time.duration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class DurationExDeserializer : JsonDeserializer<IDurationEx>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?): IDurationEx {
        return parser.valueAsString.parse().toDuration()
    }
}