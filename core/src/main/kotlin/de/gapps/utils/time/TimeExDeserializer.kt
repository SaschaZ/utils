package de.gapps.utils.time

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class TimeExDeserializer : JsonDeserializer<ITimeEx>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?): ITimeEx =
        parser.valueAsString.parse()
}