package dev.zieger.utils.time.base

import dev.zieger.utils.misc.INumber

interface IDurationHolder : Comparable<IDurationHolder>, INumber {

    override fun compareTo(other: IDurationHolder) = millis.compareTo(other.millis)

    val millis: Long
    val seconds: Long
        get() = (TimeUnit.MS to TimeUnit.S).convert(millis)
    val minutes: Long
        get() = (TimeUnit.MS to TimeUnit.M).convert(millis)
    val hours: Long
        get() = (TimeUnit.MS to TimeUnit.H).convert(millis)
    val days: Long
        get() = (TimeUnit.MS to TimeUnit.D).convert(millis)
    val weeks: Long
        get() = (TimeUnit.MS to TimeUnit.W).convert(millis)
    val months: Long
        get() = (TimeUnit.MS to TimeUnit.MONTH).convert(millis)
    val years: Long
        get() = (TimeUnit.MS to TimeUnit.YEAR).convert(millis)

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