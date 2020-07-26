package dev.zieger.utils.time.progression

import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.milliseconds
import dev.zieger.utils.time.plus
import dev.zieger.utils.time.times
import dev.zieger.utils.time.toDuration

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