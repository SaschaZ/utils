package de.gapps.utils.time.progression

import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.duration.milliseconds
import de.gapps.utils.time.base.plus
import de.gapps.utils.time.base.times
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.toTime

open class TimeExProgression(
    val start: ITimeEx,
    val end: ITimeEx,
    val step: IDurationEx
) : Iterable<ITimeEx> {

    override fun iterator(): Iterator<ITimeEx> = object : Iterator<ITimeEx> {
        private var index: Int = 0

        override fun hasNext() = start + step * (index + 1) <= end

        override fun next(): ITimeEx = start + step * ++index
    }
}

infix fun ITimeEx.until(other: ITimeEx) =
    (this..(other.millis - 1).toTime())

infix fun ClosedRange<ITimeEx>.step(step: Number) =
    TimeExProgression(start, endInclusive, step.milliseconds)

infix fun ClosedRange<ITimeEx>.step(step: IDurationEx) =
    TimeExProgression(start, endInclusive, step)