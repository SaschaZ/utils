package de.gapps.utils.time.base

interface IMillisecondHolder : Comparable<IMillisecondHolder> {

    override fun compareTo(other: IMillisecondHolder) = millis.compareTo(other.millis)

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
}

fun <T : IMillisecondHolder> List<T>.oldest(): T? = minBy { it.millis }
fun <T : IMillisecondHolder> List<T>.latest(): T? = maxBy { it.millis }
fun <T : IMillisecondHolder> List<T>.sort(): List<T> = sortedBy { it.millis }
fun <T : IMillisecondHolder> List<T>.sortDesc(): List<T> = sortedByDescending { it.millis }