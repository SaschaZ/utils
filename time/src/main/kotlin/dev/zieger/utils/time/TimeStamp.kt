@file:Suppress("unused")

package dev.zieger.utils.time

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

interface ITimeStamp : ITimeSpan {

    val timeStamp: Long
    override val timeSpan: Long get() = timeStamp

    val zone: TimeZone?

    val date: Date get() = Date(millis)

    val calendar: Calendar
        get() = (zone?.let { Calendar.getInstance(it) } ?: Calendar.getInstance()).apply { timeInMillis = millis }

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

    fun plusMonths(monthAmount: Int): ITimeStamp = if (monthAmount < 0) minusMonths(monthAmount.absoluteValue) else
        ("$dayOfMonth.${(month + monthAmount) % 12 + 1}.${year + ((month + monthAmount) / 12)}-" +
                "$hourOfDay:$minuteOfHour:$secondOfMinute.$milliOfSecond").parse(zone)

    fun minusMonths(monthAmount: Int): ITimeStamp = if (monthAmount < 0) plusMonths(monthAmount.absoluteValue) else
        ("$dayOfMonth.${(month - monthAmount) % 12 + 1}.${year + ((month - monthAmount) / 12)}-" +
                "$hourOfDay:$minuteOfHour:$secondOfMinute.$milliOfSecond").parse(zone)


    operator fun plus(other: ITimeStamp): ITimeSpan = TimeSpan(timeStamp + other.timeStamp)
    override operator fun plus(other: ITimeSpan): ITimeStamp = TimeStamp(timeStamp + other.timeSpan)
    override operator fun plus(other: Number): ITimeStamp = TimeStamp(timeStamp + other.toLong())
    operator fun minus(other: ITimeStamp): ITimeSpan = TimeSpan(timeStamp - other.timeStamp)
    override operator fun minus(other: ITimeSpan): ITimeStamp = TimeStamp(timeStamp - other.timeSpan, zone)
    override operator fun minus(other: Number): ITimeStamp = TimeStamp(timeStamp - other.toLong(), zone)
    operator fun times(other: ITimeStamp): ITimeStamp = TimeStamp(timeStamp + other.timeStamp, zone)
    override operator fun times(other: Number): ITimeStamp = TimeStamp(timeStamp * other.toLong(), zone)
    operator fun div(other: ITimeStamp): Double = timeStamp / other.timeStamp.toDouble()
    override operator fun div(other: Number): ITimeStamp = TimeStamp(timeStamp / other.toLong(), zone)
    operator fun rem(other: ITimeStamp): Double = timeStamp % other.timeStamp.toDouble()
    override operator fun rem(other: Number): ITimeStamp = TimeStamp(timeStamp % other.toLong(), zone)

    fun formatTime(
        pattern: TimeFormat = TimeFormat.COMPLETE,
        zone: TimeZone? = null,
        locale: Locale = Locale.getDefault()
    ) = SimpleDateFormat(pattern.pattern, locale).apply { zone?.let { timeZone = it } }.format(timeStamp)
}

@Serializable
open class TimeStamp(override val timeStamp: Long,
                     @Serializable(with = TimeZoneSerializer::class)
                     override val zone: TimeZone? = null) : ITimeStamp {

    companion object : TimeParseHelper() {

        var DEFAULT_TIME_ZONE: TimeZone? = null

        operator fun invoke(zone: TimeZone? = DEFAULT_TIME_ZONE) = TimeStamp(System.currentTimeMillis(), zone)

        operator fun invoke(timeStamp: Long, zone: TimeZone?) = TimeStamp(timeStamp, zone)

        operator fun invoke(string: String, zone: TimeZone? = DEFAULT_TIME_ZONE) = TimeStamp(string.stringToMillis(zone), zone)
    }

    override fun formatTime(pattern: TimeFormat, zone: TimeZone?, locale: Locale): String = super.formatTime(pattern, zone ?: this.zone, locale)

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

fun Number.toTime(zone: TimeZone? = null) = TimeStamp(toLong(), zone)
fun Number.toTime(unit: TimeUnit, zone: TimeZone) = TimeStamp(toLong().convert(unit to TimeUnit.MS), zone)
infix fun Number.toTime(unit: TimeUnit) = TimeStamp(toLong().convert(unit to TimeUnit.MS))
val ITimeSpan.time: ITimeStamp get() = timeSpan.toTime()

val UTC: TimeZone = TimeZone.getTimeZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC))

internal fun Int.daysInMonth(year: Int): Int = when (this) {
    2 -> if (year.isLeap) 29 else 28
    1, 3, 5, 7, 8, 10, 12 -> 31
    else -> 30
}

private val Int.isLeap: Boolean get() = this % 4 == 0 && this % 100 != 0

