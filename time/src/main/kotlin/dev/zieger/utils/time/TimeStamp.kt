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

    val calendar: Calendar
        get() = (zone?.let { Calendar.getInstance(it) } ?: Calendar.getInstance()).apply {
            timeInMillis = millis.toLong()
        }

    /**
     * @see [Calendar.get]
     */
    fun getCalendarField(field: Int) = calendar.get(field)

    val year: Int get() = getCalendarField(Calendar.YEAR)
    val month: Int get() = getCalendarField(Calendar.MONTH)
    val weekOfYear: Int get() = getCalendarField(Calendar.WEEK_OF_YEAR)
    val dayOfYear: Int get() = getCalendarField(Calendar.DAY_OF_YEAR)
    val dayOfMonth: Int get() = getCalendarField(Calendar.DAY_OF_MONTH)
    val dayOfWeek: Int get() = getCalendarField(Calendar.DAY_OF_WEEK)
    val hourOfDay: Int get() = getCalendarField(Calendar.HOUR_OF_DAY)
    val minuteOfHour: Int get() = getCalendarField(Calendar.MINUTE)
    val secondOfMinute: Int get() = getCalendarField(Calendar.SECOND)
    val milliOfSecond: Int get() = getCalendarField(Calendar.MILLISECOND)

    val daysInMonth: Int get() = (month + 1).daysInMonth(year)

    val startOfYear: ITimeStamp get() = "1.1.$year".parse(zone)
    val startOfMonth: ITimeStamp get() = "1.${month + 1}.$year".parse(zone)
    val startOfDay: ITimeStamp get() = "$dayOfMonth.${month + 1}.$year".parse(zone)
    val startOfHour: ITimeStamp get() = "$dayOfMonth.${month + 1}.$year-$hourOfDay:00:00".parse(zone)
    val startOfMinute: ITimeStamp get() = "$dayOfMonth.${month + 1}.$year-$hourOfDay:$minuteOfHour:00".parse(zone)
    val startOfSecond: ITimeStamp get() = "$dayOfMonth.${month + 1}.$year-$hourOfDay:$minuteOfHour:$secondOfMinute".parse(zone)

    fun plusMonths(monthAmount: Int): TimeStamp = if (monthAmount < 0) minusMonths(monthAmount.absoluteValue) else
        ("$dayOfMonth.${(month + monthAmount) % 12 + 1}.${year + ((month + monthAmount) / 12)}-" +
                "$hourOfDay:$minuteOfHour:$secondOfMinute.$milliOfSecond").parse(zone)

    fun minusMonths(monthAmount: Int): TimeStamp = if (monthAmount < 0) plusMonths(monthAmount.absoluteValue) else
        ("$dayOfMonth.${(month - monthAmount) % 12 + 1}.${year + ((month - monthAmount) / 12)}-" +
                "$hourOfDay:$minuteOfHour:$secondOfMinute.$milliOfSecond").parse(zone)


    operator fun plus(other: ITimeStamp): TimeSpan = TimeSpan(timeStamp + other.timeStamp)
    operator fun plus(other: ITimeSpan): TimeStamp = TimeStamp(timeStamp + other.timeSpan, zone)
    operator fun plus(other: Number): TimeStamp = TimeStamp(timeStamp + other.toDouble(), zone)

    operator fun minus(other: ITimeStamp): TimeSpan = TimeSpan(timeStamp - other.timeStamp)
    operator fun minus(other: ITimeSpan): TimeStamp = TimeStamp(timeStamp - other.timeSpan, zone)
    operator fun minus(other: Number): TimeStamp = TimeStamp(timeStamp - other.toDouble(), zone)

    operator fun times(other: ITimeStamp): TimeStamp = TimeStamp(timeStamp + other.timeStamp, zone)
    operator fun times(other: ITimeSpan): TimeStamp = TimeStamp(timeStamp * other.timeSpan, zone)
    operator fun times(other: Number): TimeStamp = TimeStamp(timeStamp * other.toDouble(), zone)

    operator fun div(other: ITimeStamp): Double = timeStamp / other.timeStamp.toDouble()
    operator fun div(other: ITimeSpan): Double = timeStamp / other.timeSpan.toDouble()
    operator fun div(other: Number): TimeStamp = TimeStamp(timeStamp / other.toDouble(), zone)

    operator fun rem(other: ITimeStamp): Double = timeStamp % other.timeStamp.toDouble()
    operator fun rem(other: ITimeSpan): Double = timeStamp % other.timeSpan.toDouble()
    operator fun rem(other: Number): TimeStamp = TimeStamp(timeStamp % other.toDouble(), zone)

    fun normalize(value: ITimeSpan): TimeStamp = this - this % value

    fun formatTime(
        pattern: TimeFormat = TimeFormat.COMPLETE,
        zone: TimeZone? = DEFAULT_TIME_ZONE,
        locale: Locale = DEFAULT_LOCALE
    ): String = SimpleDateFormat(pattern.pattern, locale).apply { zone?.let { timeZone = it } }.format(timeStamp)
}

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

