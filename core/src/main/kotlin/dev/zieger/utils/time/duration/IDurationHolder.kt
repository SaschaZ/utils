package dev.zieger.utils.time.duration

import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.convert

interface IDurationHolder : Comparable<IDurationHolder> {

    override fun compareTo(other: IDurationHolder) = millis.compareTo(other.millis)

    val millis: Long
    val seconds: Long
        get() = (TimeUnit.MILLI to TimeUnit.SECOND).convert(millis)
    val minutes: Long
        get() = (TimeUnit.MILLI to TimeUnit.MINUTE).convert(millis)
    val hours: Long
        get() = (TimeUnit.MILLI to TimeUnit.HOUR).convert(millis)
    val days: Long
        get() = (TimeUnit.MILLI to TimeUnit.DAY).convert(millis)
    val weeks: Long
        get() = (TimeUnit.MILLI to TimeUnit.WEEK).convert(millis)
    val months: Long
        get() = (TimeUnit.MILLI to TimeUnit.MONTH).convert(millis)
    val years: Long
        get() = (TimeUnit.MILLI to TimeUnit.YEAR).convert(millis)

    val notZero: Boolean
        get() = millis != 0L

    val positive: Boolean
        get() = millis > 0L

    val negative: Boolean
        get() = millis < 0L
}

fun <T : IDurationHolder> List<T>.oldest(): T? = minBy { it.millis }
fun <T : IDurationHolder> List<T>.latest(): T? = maxBy { it.millis }
fun <T : IDurationHolder> List<T>.sort(): List<T> = sortedBy { it.millis }
fun <T : IDurationHolder> List<T>.sortDesc(): List<T> = sortedByDescending { it.millis }