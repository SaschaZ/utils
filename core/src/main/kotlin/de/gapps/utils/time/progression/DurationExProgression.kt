package de.gapps.utils.time.progression

import de.gapps.utils.time.base.TimeUnit
import de.gapps.utils.time.base.plus
import de.gapps.utils.time.base.times
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.milliseconds
import de.gapps.utils.time.duration.toDuration

open class DurationExProgression(
    val start: IDurationEx,
    val end: IDurationEx,
    val step: IDurationEx
) : Iterable<IDurationEx> {

    override fun iterator(): Iterator<IDurationEx> = object : Iterator<IDurationEx> {
        private var index: Int = 0

        override fun hasNext() = (start + step * (index + 1)) <= end

        override fun next(): IDurationEx = start + (step * ++index)
    }
}

infix fun IDurationEx.until(other: IDurationEx) =
    (this..(other.millis - 1).toDuration(TimeUnit.MS))

infix fun ClosedRange<IDurationEx>.step(step: Number) =
    DurationExProgression(start, endInclusive, step.milliseconds)

infix fun ClosedRange<IDurationEx>.step(step: IDurationEx) =
    DurationExProgression(start, endInclusive, step)