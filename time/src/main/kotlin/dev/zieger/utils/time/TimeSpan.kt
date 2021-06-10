@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.time.TimeUnit.*
import kotlinx.serialization.Serializable

interface ITimeSpan : Comparable<ITimeSpan>, Comparator<ITimeSpan> {

    val timeSpan: Long

    val millis: Long
        get() = timeSpan
    val seconds: Long
        get() = millis.convert(MS to S)
    val minutes: Long
        get() = millis.convert(MS to M)
    val hours: Long
        get() = millis.convert(MS to H)
    val days: Long
        get() = millis.convert(MS to D)
    val weeks: Long
        get() = millis.convert(MS to W)
    val months: Long
        get() = millis.convert(MS to MONTH)
    val years: Long
        get() = millis.convert(MS to YEAR)

    val notZero: Boolean
        get() = millis != 0L

    val positive: Boolean
        get() = millis > 0L

    val negative: Boolean
        get() = millis < 0L

    operator fun plus(other: ITimeSpan): ITimeSpan = TimeSpan(timeSpan + other.timeSpan)
    operator fun plus(other: Number): ITimeSpan = TimeSpan(timeSpan + other.toLong())
    operator fun minus(other: ITimeSpan): ITimeSpan = TimeSpan(timeSpan - other.timeSpan)
    operator fun minus(other: Number): ITimeSpan = TimeSpan(timeSpan - other.toLong())
    operator fun times(other: ITimeSpan): ITimeSpan = TimeSpan(timeSpan * other.timeSpan)
    operator fun times(other: Number): ITimeSpan = TimeSpan(timeSpan * other.toLong())
    operator fun div(other: ITimeSpan): Double = timeSpan / other.timeSpan.toDouble()
    operator fun div(other: Number): ITimeSpan = TimeSpan(timeSpan / other.toLong())
    operator fun rem(other: ITimeSpan): Double = timeSpan % other.timeSpan.toDouble()
    operator fun rem(other: Number): ITimeSpan = TimeSpan(timeSpan % other.toLong())

    operator fun compareTo(other: Number) = timeSpan.compareTo(other.toLong())
    override operator fun compareTo(other: ITimeSpan) = compare(this, other)

    override fun compare(p0: ITimeSpan, p1: ITimeSpan) = p0.timeSpan.compareTo(p1.timeSpan)

    fun formatSpan(
        vararg entities: TimeUnit = values(),
        maxEntities: Int = values().size,
        sameLength: Boolean = false
    ): String {
        var entityCnt = 0
        var millisTmp = millis
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
open class TimeSpan(override val timeSpan: Long) : ITimeSpan {
    override fun toString(): String = formatSpan()
    override fun hashCode(): Int = timeSpan.hashCode()
    override fun equals(other: Any?): Boolean = (other as? ITimeSpan)?.millis == millis
}

val Number.millis: ITimeSpan get() = TimeSpan(toLong())
val Number.seconds: ITimeSpan get() = TimeSpan(toLong().convert(SECOND to MILLI))
val Number.minutes: ITimeSpan get() = TimeSpan(toLong().convert(MINUTE to MILLI))
val Number.hours: ITimeSpan get() = TimeSpan(toLong().convert(HOUR to MILLI))
val Number.days: ITimeSpan get() = TimeSpan(toLong().convert(DAY to MILLI))
val Number.weeks: ITimeSpan get() = TimeSpan(toLong().convert(WEEK to MILLI))
val Number.months: ITimeSpan get() = TimeSpan(toLong().convert(MONTH to MILLI))
val Number.years: ITimeSpan get() = TimeSpan(toLong().convert(YEAR to MILLI))

operator fun Number.compareTo(other: ITimeSpan) = toLong().compareTo(other.timeSpan)

fun <T : ITimeSpan> min(vararg values: T?): T = values.filterNotNull().minOrNull()!!
fun <T : ITimeSpan> max(vararg values: T?): T = values.filterNotNull().maxOrNull()!!

