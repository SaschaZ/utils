package dev.zieger.utils.time

import dev.zieger.utils.time.base.IMillisecondHolder
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.MS
import dev.zieger.utils.time.base.toMillis
import kotlinx.serialization.*
import java.util.*

@Serializable
open class TimeEx(
    override val millis: Long = System.currentTimeMillis(),
    override val zone: TimeZone = TimeZone.getDefault()
) : ITimeEx {

    @Serializer(forClass = TimeEx::class)
    companion object : TimeParseHelper(), KSerializer<TimeEx> {
        @ImplicitReflectionSerializer
        override val descriptor: SerialDescriptor = SerialDescriptor("TimeEx") {
            element<String>("millis")
            element<String>("zone")
        }

        @ImplicitReflectionSerializer
        override fun deserialize(decoder: Decoder): TimeEx {
            var millis: Long = -1
            lateinit var zone: TimeZone

            decoder.beginStructure(descriptor).apply {
                loop@ while (true) {
                    when (val i = decodeElementIndex(descriptor)) {
                        CompositeDecoder.READ_DONE -> break@loop
                        0 -> millis = decodeLongElement(descriptor, i)
                        1 -> zone = TimeZone.getTimeZone(decodeStringElement(descriptor, i))
                        else -> throw SerializationException("Unknown index $i")
                    }
                }
            }
            return TimeEx(millis, zone)
        }

        @ImplicitReflectionSerializer
        override fun serialize(encoder: Encoder, value: TimeEx) {
            encoder.beginStructure(descriptor).apply {
                encodeLongElement(descriptor, 0, value.millis)
                encodeStringElement(descriptor, 1, value.zone.id)
                endStructure(descriptor)
            }
        }
    }

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