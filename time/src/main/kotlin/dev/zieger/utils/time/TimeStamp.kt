@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.time.TimeStamp.Companion.DEFAULT_LOCALE
import dev.zieger.utils.time.TimeStamp.Companion.DEFAULT_TIME_ZONE
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.math.absoluteValue

interface ITimeStamp : ITimeSpanBase<ITimeStamp> {

    val timeStamp: Double
    override val timeSpan: Double get() = timeStamp

    val zone: TimeZone?

    val date: Date get() = Date(millis.toLong())

    fun plusMonths(monthAmount: Int): TimeStamp = if (monthAmount < 0) minusMonths(monthAmount.absoluteValue) else
        ("$dayOfMonth.${(monthOfYear + monthAmount) % 12 + 1}.${year + ((monthOfYear + monthAmount) / 12)}-" +
                "$hourOfDay:$minuteOfHour:$secondOfMinute.$milliOfSecond").parse(zone)

    fun minusMonths(monthAmount: Int): TimeStamp = if (monthAmount < 0) plusMonths(monthAmount.absoluteValue) else
        ("$dayOfMonth.${(monthOfYear - monthAmount) % 12 + 1}.${year + ((monthOfYear - monthAmount) / 12)}-" +
                "$hourOfDay:$minuteOfHour:$secondOfMinute.$milliOfSecond").parse(zone)


    operator fun plus(other: ITimeStamp): TimeSpan = TimeSpan(timeStamp + other.timeStamp)
    operator fun plus(other: ITimeSpan): TimeStamp = TimeStamp(timeStamp + other.timeSpan, zone)
    operator fun plus(other: Number): TimeStamp = TimeStamp(timeStamp + other.toDouble(), zone)
    fun plusDouble(other: ITimeStamp): Double = (this + other).timeSpan
    fun plusDouble(other: ITimeSpan): Double = (this + other).timeSpan
    fun plusDouble(other: Number): Double = (this + other).timeSpan

    operator fun minus(other: ITimeStamp): TimeSpan = TimeSpan(timeStamp - other.timeStamp)
    operator fun minus(other: ITimeSpan): TimeStamp = TimeStamp(timeStamp - other.timeSpan, zone)
    operator fun minus(other: Number): TimeStamp = TimeStamp(timeStamp - other.toDouble(), zone)
    fun minusDouble(other: ITimeStamp): Double = (this - other).timeSpan
    fun minusDouble(other: ITimeSpan): Double = (this - other).timeSpan
    fun minusDouble(other: Number): Double = (this - other).timeSpan

    operator fun times(other: ITimeStamp): ITimeStamp = TimeStamp(timeStamp + other.timeStamp, zone)
    operator fun times(other: ITimeSpan): TimeStamp = TimeStamp(timeStamp * other.timeSpan, zone)
    operator fun times(other: Number): TimeStamp = TimeStamp(timeStamp * other.toDouble(), zone)
    fun timesDouble(other: ITimeStamp): Double = (this * other).timeSpan
    fun timesDouble(other: ITimeSpan): Double = (this * other).timeSpan
    fun timesDouble(other: Number): Double = (this * other).timeSpan

    operator fun div(other: ITimeStamp): TimeSpan = TimeSpan(timeStamp / other.timeStamp)
    operator fun div(other: ITimeSpan): TimeSpan = TimeSpan(timeStamp / other.millis)
    operator fun div(other: Number): TimeSpan = TimeSpan(timeStamp / other.toDouble())
    fun divDouble(other: ITimeStamp): Double = (this / other).timeSpan
    fun divDouble(other: ITimeSpan): Double = (this / other).timeSpan
    fun divDouble(other: Number): Double = (this / other).timeSpan

    operator fun rem(other: ITimeStamp): TimeSpan = TimeSpan(timeStamp % other.timeStamp)
    operator fun rem(other: ITimeSpan): TimeSpan = TimeSpan(timeStamp % other.millis)
    operator fun rem(other: Number): TimeSpan = TimeSpan(timeStamp % other.toDouble())
    fun remDouble(other: ITimeStamp): Double = (this % other).timeSpan
    fun remDouble(other: ITimeSpan): Double = (this % other).timeSpan
    fun remDouble(other: Number): Double = (this % other).timeSpan

    fun normalize(value: ITimeSpan): TimeStamp = this - this.remDouble(this)

    fun formatTime(
        pattern: TimeFormat = TimeFormat.COMPLETE,
        zone: TimeZone? = DEFAULT_TIME_ZONE,
        locale: Locale = DEFAULT_LOCALE
    ): String = SimpleDateFormat(pattern.pattern, locale).apply { zone?.let { timeZone = it } }.format(timeStamp)
}

operator fun Number.plus(other: ITimeStamp): TimeStamp = TimeStamp(other.plusDouble(this))
operator fun Number.minus(other: ITimeStamp): TimeStamp = TimeStamp(toDouble() - other.timeSpan)
operator fun Number.times(other: ITimeStamp): TimeStamp = TimeStamp(other.timesDouble(this))
operator fun Number.div(other: ITimeStamp): TimeStamp = TimeStamp(other.divDouble(this))
operator fun Number.rem(other: ITimeStamp): TimeStamp = TimeStamp(other.remDouble(this))

@Serializable
open class TimeStamp(
    override val timeStamp: Double,
    @Serializable(with = TimeZoneSerializer::class)
    override val zone: TimeZone? = DEFAULT_TIME_ZONE
) : ITimeStamp {

    companion object : TimeParseHelper() {

        var DEFAULT_TIME_ZONE: TimeZone? = null
        var DEFAULT_LOCALE: Locale = Locale.getDefault()

        operator fun invoke(zone: TimeZone? = DEFAULT_TIME_ZONE) =
            TimeStamp(System.currentTimeMillis().toDouble(), zone)

        operator fun invoke(timeStamp: Double, zone: TimeZone?) = TimeStamp(timeStamp, zone)

        operator fun invoke(string: String, zone: TimeZone? = DEFAULT_TIME_ZONE) =
            TimeStamp(string.stringToMillis(zone).toDouble(), zone)
    }

    override fun formatTime(pattern: TimeFormat, zone: TimeZone?, locale: Locale): String =
        super.formatTime(pattern, zone ?: this.zone, locale)

    override fun compareTo(other: ITimeStamp): Int = compare(this, other)

    override fun toString(): String = formatTime()
    override fun hashCode(): Int = timeStamp.hashCode() + (zone?.hashCode() ?: 0)
    override fun equals(other: Any?): Boolean =
        (other as? ITimeStamp)?.let { it.timeStamp == timeStamp && it.zone == zone } == true
}

object TimeZoneSerializer : KSerializer<TimeZone> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("timeZone", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TimeZone) =
        encoder.encodeString(value.id)

    override fun deserialize(decoder: Decoder): TimeZone =
        TimeZone.getTimeZone(decoder.decodeString())
}

val timeSerializerModule = SerializersModule {
    polymorphic(ITimeSpan::class, TimeSpan::class, TimeSpan.serializer())
    polymorphic(ITimeStamp::class, TimeStamp::class, TimeStamp.serializer())
}

fun Number.toTime(unit: TimeUnit = TimeUnit.MILLI, zone: TimeZone? = DEFAULT_TIME_ZONE): ITimeStamp =
    TimeStamp(toLong().convert(unit to TimeUnit.MS), zone)

fun ITimeSpan.toTime(unit: TimeUnit = TimeUnit.MILLI, zone: TimeZone? = DEFAULT_TIME_ZONE): ITimeStamp =
    timeSpan.toTime(unit, zone)

val UTC: TimeZone = TimeZone.getTimeZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC))

internal fun Int.daysInMonth(year: Int): Int = when (this) {
    2 -> if (year.isLeap) 29 else 28
    1, 3, 5, 7, 8, 10, 12 -> 31
    else -> 30
}

private val Int.isLeap: Boolean get() = this % 4 == 0 && this % 100 != 0

val ClosedRange<out ITimeStamp>.span: ITimeSpan get() = endInclusive - start

