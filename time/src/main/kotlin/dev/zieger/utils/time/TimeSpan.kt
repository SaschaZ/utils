@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.time.TimeUnit.*
import kotlinx.serialization.Serializable

interface ITimeSpan : ITimeSpanBase<ITimeSpan> {
    override fun compareTo(other: ITimeSpan): Int = compare(this, other)

    operator fun plus(other: ITimeSpan): TimeSpan = TimeSpan(timeSpan + other.timeSpan)
    operator fun plus(other: Number): TimeSpan = TimeSpan(timeSpan + other.toDouble())
    operator fun minus(other: ITimeSpan): TimeSpan = TimeSpan(timeSpan - other.timeSpan)
    operator fun minus(other: Number): TimeSpan = TimeSpan(timeSpan - other.toLong())
    operator fun times(other: ITimeSpan): TimeSpan = TimeSpan(timeSpan * other.timeSpan)
    operator fun div(other: ITimeSpan): Double = timeSpan / other.timeSpan.toDouble()
    operator fun times(other: Number): TimeSpan = TimeSpan(timeSpan * other.toLong())
    operator fun div(other: Number): TimeSpan = TimeSpan(timeSpan / other.toLong())
    operator fun rem(other: ITimeSpan): Double = timeSpan % other.timeSpan.toDouble()
    operator fun rem(other: Number): TimeSpan = TimeSpan(timeSpan % other.toLong())
}

interface ITimeSpanBase<T : ITimeSpanBase<T>> : Comparable<T>, Comparator<T> {

    val timeSpan: Double

    val millis: Double
        get() = timeSpan
    val millisLong: Long
        get() = millis.toLong()
    val seconds: Double
        get() = millis.convert(MS to S)
    val secondsLong: Long
        get() = seconds.toLong()
    val minutes: Double
        get() = millis.convert(MS to M)
    val minutesLong: Long
        get() = minutes.toLong()
    val hours: Double
        get() = millis.convert(MS to H)
    val hoursLong: Long
        get() = hours.toLong()
    val days: Double
        get() = millis.convert(MS to D)
    val daysLong: Long
        get() = days.toLong()
    val weeks: Double
        get() = millis.convert(MS to W)
    val weeksLong: Long
        get() = weeks.toLong()
    val months: Double
        get() = millis.convert(MS to MONTH)
    val monthsLong: Long
        get() = months.toLong()
    val years: Double
        get() = millis.convert(MS to YEAR)
    val yearsLong: Long
        get() = years.toLong()

    val isZero: Boolean
        get() = millis == 0.0

    val notZero: Boolean
        get() = !isZero

    val positive: Boolean
        get() = millis > 0L

    val negative: Boolean
        get() = millis < 0L

    operator fun compareTo(other: Number) = timeSpan.compareTo(other.toDouble())

    override fun compare(p0: T, p1: T) = p0.timeSpan.compareTo(p1.timeSpan)

    fun formatSpan(
        vararg entities: TimeUnit = values(),
        maxEntities: Int = values().size,
        sameLength: Boolean = false
    ): String {
        var entityCnt = 0
        var millisTmp = millisLong
        return entities.sortedByDescending { it.factorMillis }.mapNotNull { unit ->
            val factorMillis = unit.factorMillis
            val div = millisTmp / factorMillis
            val mod = millisTmp % factorMillis
            millisTmp = mod
            if (div > 0L && entityCnt < maxEntities) {
                entityCnt++
                "${"%${if (sameLength) "3" else ""}d".format(div)}${unit.shortChar}"
            } else null
        }.joinToString(" ")
    }
}

@Serializable
open class TimeSpan(override val timeSpan: Double) : ITimeSpan {
    override fun toString(): String = formatSpan()
    override fun hashCode(): Int = timeSpan.hashCode()
    override fun equals(other: Any?): Boolean = (other as? ITimeSpan)?.millis == millis
}

val Number.millis: TimeSpan get() = TimeSpan(toDouble())
val Number.seconds: TimeSpan get() = TimeSpan(toDouble().convert(SECOND to MILLI))
val Number.minutes: TimeSpan get() = TimeSpan(toDouble().convert(MINUTE to MILLI))
val Number.hours: TimeSpan get() = TimeSpan(toDouble().convert(HOUR to MILLI))
val Number.days: TimeSpan get() = TimeSpan(toDouble().convert(DAY to MILLI))
val Number.weeks: TimeSpan get() = TimeSpan(toDouble().convert(WEEK to MILLI))
val Number.months: TimeSpan get() = TimeSpan(toDouble().convert(MONTH to MILLI))
val Number.years: TimeSpan get() = TimeSpan(toDouble().convert(YEAR to MILLI))

operator fun Number.compareTo(other: ITimeSpan) = toDouble().compareTo(other.timeSpan)

fun <T : Comparable<T>> min(vararg values: T?): T = values.filterNotNull().minByOrNull { it }!!
fun <T : Comparable<T>> max(vararg values: T?): T = values.filterNotNull().maxByOrNull { it }!!

suspend fun delay(duration: ITimeSpan) = kotlinx.coroutines.delay(duration.millis.toLong())

val ITimeSpan.abs: ITimeSpan get() = if (negative) this * -1 else this

public inline fun <T> Iterable<T>.sumOf(selector: (T) -> ITimeSpan?): ITimeSpan {
    var sum: ITimeSpan = 0.millis
    for (element in this) {
        sum += selector(element) ?: 0.millis
    }
    return sum
}


