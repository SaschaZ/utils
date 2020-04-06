package de.gapps.utils.time

import de.gapps.utils.time.base.IMillisecondHolder
import de.gapps.utils.time.base.TimeUnit
import de.gapps.utils.time.base.TimeUnit.MS
import de.gapps.utils.time.base.toMillis
import kotlinx.serialization.*
import java.util.*

@Serializer(forClass = TimeZone::class)
class TimeZoneSerializer : KSerializer<TimeZone> {
    override val descriptor: SerialDescriptor = SerialDescriptor("TimeZone")

    override fun deserialize(decoder: Decoder): TimeZone {
        var zone: TimeZone? = null
        decoder.beginStructure(descriptor).apply {
            zone = TimeZone.getTimeZone(decodeStringElement(descriptor, 0))
            endStructure(descriptor)
        }
        return zone ?: throw IllegalArgumentException("Could not deserialize TimeZone")
    }

    override fun serialize(encoder: Encoder, value: TimeZone) {
        encoder.beginStructure(descriptor).apply {
            encodeStringElement(descriptor, 0, value.id)
            endStructure(descriptor)
        }
    }
}

@Serializable
open class TimeEx(
    override val millis: Long = System.currentTimeMillis(),
    @Serializable(with = TimeZoneSerializer::class)
    override val zone: TimeZone = TimeZone.getDefault()
) : ITimeEx {

    companion object : TimeParseHelper()

    constructor(value: Number, timeUnit: TimeUnit = MS, timeZone: TimeZone = TimeZone.getDefault()) :
            this(value.toLong().toMillis(timeUnit), timeZone)

    constructor(source: String, timeZone: TimeZone = TimeZone.getDefault()) :
            this(source.stringToMillis(timeZone), timeZone)

    constructor(date: Date, timeZone: TimeZone = TimeZone.getDefault()) :
            this(date.time, timeZone)

    override fun toString() = formatTime(DateFormat.COMPLETE)
    override fun equals(other: Any?) = millis == (other as? ITimeEx)?.millis && zone == other.zone
    override fun hashCode() = millis.hashCode() + zone.hashCode() + javaClass.hashCode()
}


fun Number.toTime() = toTime(MS)
infix fun Number.toTime(unit: TimeUnit) = TimeEx(this, unit)

val IMillisecondHolder.time: ITimeEx
    get() = millis.toTime()