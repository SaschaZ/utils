package dev.zieger.utils.time.progression

import dev.zieger.utils.time.ITimeSpan
import dev.zieger.utils.time.millis

open class TimeSpanProgression(
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

infix fun <T : ITimeSpan, C : ClosedRange<T>> C.step(step: Number) =
    TimeSpanProgression(start, endInclusive, step.millis)

infix fun <T : ITimeSpan, C : ClosedRange<T>> C.step(step: ITimeSpan) =
    TimeSpanProgression(start, endInclusive, step)