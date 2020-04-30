package dev.zieger.utils.time

import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.MS
import dev.zieger.utils.time.base.toMillis
import dev.zieger.utils.time.duration.IDurationHolder
import kotlinx.serialization.*
import java.util.*

@Serializable
open class TimeEx(
    override val millis: Long = System.currentTimeMillis(),
    override val nanos: Long = System.nanoTime(),
    override val zone: TimeZone = TimeZone.getDefault()
) : ITimeEx {

    @Serializer(forClass = TimeEx::class)
    companion object : TimeParseHelper(), KSerializer<TimeEx> {
        @ImplicitReflectionSerializer
        override val descriptor: SerialDescriptor = SerialDescriptor("TimeEx") {
            element<String>("millis")
            element<String>("nanos")
            element<String>("zone")
        }

        @ImplicitReflectionSerializer
        override fun deserialize(decoder: Decoder): TimeEx {
            var millis: Long = -1L
            var nanos: Long = -1L
            lateinit var zone: TimeZone

            decoder.beginStructure(descriptor).apply {
                loop@ while (true) {
                    when (val i = decodeElementIndex(descriptor)) {
                        CompositeDecoder.READ_DONE -> break@loop
                        0 -> millis = decodeLongElement(descriptor, i)
                        1 -> nanos = decodeLongElement(descriptor, i)
                        2 -> zone = TimeZone.getTimeZone(decodeStringElement(descriptor, i))
                        else -> throw SerializationException("Unknown index $i")
                    }
                }
            }
            return TimeEx(millis, nanos, zone)
        }

        @ImplicitReflectionSerializer
        override fun serialize(encoder: Encoder, value: TimeEx) {
            encoder.beginStructure(descriptor).apply {
                encodeLongElement(descriptor, 0, value.millis)
                encodeLongElement(descriptor, 1, value.nanos)
                encodeStringElement(descriptor, 2, value.zone.id)
                endStructure(descriptor)
            }
        }
    }

    constructor(value: Number, timeUnit: TimeUnit = MS, timeZone: TimeZone = TimeZone.getDefault()) :
            this(value.toLong().toMillis(timeUnit), 0L, timeZone)

    constructor(source: String, timeZone: TimeZone = TimeZone.getDefault()) :
            this(source.stringToMillis(timeZone), 0L, timeZone)

    constructor(date: Date, timeZone: TimeZone = TimeZone.getDefault()) :
            this(date.time, 0L, timeZone)

    override fun toString() = formatTime(DateFormat.COMPLETE)
    override fun equals(other: Any?) = millis == (other as? ITimeEx)?.millis
            && nanos == other.nanos
            && zone == other.zone

    override fun hashCode() = millis.hashCode() + nanos.hashCode() + zone.hashCode() + javaClass.hashCode()
}


fun Number.toTime() = toTime(MS)
infix fun Number.toTime(unit: TimeUnit) = TimeEx(this, unit)

val IDurationHolder.time: ITimeEx
    get() = millis.toTime()