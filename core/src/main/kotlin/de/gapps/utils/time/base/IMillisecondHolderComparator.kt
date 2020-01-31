package de.gapps.utils.time.base

interface IMillisecondHolderComparator : Comparable<IMillisecondHolder>, Comparator<IMillisecondHolder>,
    IMillisecondHolder {

    operator fun compareTo(other: Number) = millis.compareTo(other.toLong())
    override operator fun compareTo(other: IMillisecondHolder) = compare(this, other)

    override fun compare(p0: IMillisecondHolder, p1: IMillisecondHolder) = p0.millis.compareTo(p1.millis)
}

operator fun Number.compareTo(other: IMillisecondHolder) = toLong().compareTo(other.millis)

fun <T : IMillisecondHolder> min(a: T, b: T): T = if (a < b) a else b
fun <T : IMillisecondHolder> max(a: T, b: T): T = if (a > b) a else b
