package de.gapps.utils.time.base

interface IMillisecondHolderComparator : Comparable<IMillisecondHolder>, Comparator<IMillisecondHolder>,
    IMillisecondHolder {

    operator fun IMillisecondHolder.compareTo(other: Number) = millis.compareTo(other.toLong())
    operator fun Number.compareTo(other: IMillisecondHolder) = toLong().compareTo(other.millis)
    override operator fun compareTo(other: IMillisecondHolder) = compare(this, other)
    override fun compare(p0: IMillisecondHolder, p1: IMillisecondHolder) = p0.millis.compareTo(p1.millis)
}