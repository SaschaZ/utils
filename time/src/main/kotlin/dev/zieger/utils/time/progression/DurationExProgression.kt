package dev.zieger.utils.time.progression

import dev.zieger.utils.time.ITimeSpan
import dev.zieger.utils.time.millis

open class DurationExProgression(
    val start: ITimeSpan,
    val end: ITimeSpan,
    val step: ITimeSpan
) : Iterable<ITimeSpan> {

    override fun iterator(): Iterator<ITimeSpan> = object : Iterator<ITimeSpan> {
        private var index: Int = 0

        override fun hasNext() = (start + step * (index + 1)) <= end

        override fun next(): ITimeSpan = start + (step * ++index)
    }
}

infix fun ITimeSpan.until(other: ITimeSpan) =
    (this..(other.millis - 1).millis)

infix fun ClosedRange<ITimeSpan>.step(step: Number) =
    DurationExProgression(start, endInclusive, step.millis)

infix fun ClosedRange<ITimeSpan>.step(step: ITimeSpan) =
    DurationExProgression(start, endInclusive, step)