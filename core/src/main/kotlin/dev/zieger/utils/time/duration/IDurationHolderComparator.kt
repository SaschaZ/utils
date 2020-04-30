package dev.zieger.utils.time.duration

interface IDurationHolderComparator : Comparable<IDurationHolder>, Comparator<IDurationHolder>,
    IDurationHolder {

    operator fun compareTo(other: Number) = millis.compareTo(other.toLong())
    override operator fun compareTo(other: IDurationHolder) = compare(this, other)

    override fun compare(p0: IDurationHolder, p1: IDurationHolder) = p0.millis.compareTo(p1.millis)
}

operator fun Number.compareTo(other: IDurationHolder) = toLong().compareTo(other.millis)

fun <T : IDurationHolder> min(a: T, b: T): T = if (a < b) a else b
fun <T : IDurationHolder> max(a: T, b: T): T = if (a > b) a else b
