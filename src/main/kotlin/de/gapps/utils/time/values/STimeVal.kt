package de.gapps.utils.time.values

import de.gapps.utils.time.base.IMillisecondHolder
import de.gapps.utils.time.time


fun <T, IV : ITimeVal<T>> List<IV>.values() = map { it.value }

sealed class STimeVal<out T> : ITimeVal<T>, IMillisecondHolder {

    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?) = (other as? ITimeVal<T>)?.let { it == time } == true

    override fun hashCode() = value.hashCode() + time.hashCode()

    override fun toString() = "TimeVal(time: $time; value: $value)"

    class TimeVal<out T>(
        override val value: T,
        ts: IMillisecondHolder
    ) : STimeVal<T>(), IMillisecondHolder by ts

    open class TripleLineValue(
        val topVal: Double,
        val midVal: Double,
        val bottomVal: Double
    ) : List<Double> by listOf(topVal, midVal, bottomVal)

    class OhclVal(override val value: IOhclVal) : STimeVal<IOhclVal>(), IOhclVal by value
}