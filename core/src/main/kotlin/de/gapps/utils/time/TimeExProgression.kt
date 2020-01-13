package de.gapps.utils.time

import de.gapps.utils.time.base.IMillisecondArithmetic
import de.gapps.utils.time.duration.milliseconds

typealias MilliHolder = IMillisecondArithmetic<*, *>

class TimeExProgression(
    val start: MilliHolder,
    val end: MilliHolder,
    val step: MilliHolder
) : Iterable<MilliHolder> {

    override fun iterator(): Iterator<MilliHolder> = object : Iterator<MilliHolder> {
        private var index = 0

        override fun hasNext() = (start + step * (index + 1)) <= end

        override fun next(): ITimeEx = (start + (step * ++index)).time
    }
}

infix fun <T : MilliHolder> T.until(other: T) =
    (this..(other.millis - 1).toTime())

infix fun <T : MilliHolder> ClosedRange<T>.step(step: Number) =
    TimeExProgression(start, endInclusive, step.milliseconds)

infix fun <T : MilliHolder> ClosedRange<T>.step(step: MilliHolder) =
    TimeExProgression(start, endInclusive, step)