package dev.zieger.utils.time.progression

import dev.zieger.utils.time.ITimeSpan
import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.millis

open class TimeStampProgression(
    val start: ITimeStamp,
    val end: ITimeStamp,
    val step: ITimeSpan
) : Iterable<ITimeStamp> {

    override fun iterator(): Iterator<ITimeStamp> = object : Iterator<ITimeStamp> {
        private var index: Int = 0

        override fun hasNext() = start + step * index <= end

        override fun next(): ITimeStamp = start + step * index++
    }
}

infix fun <T : ITimeStamp, C : ClosedRange<T>> C.step(step: Number) = step(step.millis)

infix fun <T : ITimeStamp, C : ClosedRange<T>> C.step(step: ITimeSpan) =
    TimeStampProgression(start, endInclusive, step)

infix fun <T : ITimeStamp, C : ClosedRange<T>> C.stepScaled(step: ITimeSpan) =
    (start - start % step..endInclusive + (step - endInclusive % step)).step(step)

val <T : ITimeStamp, C : ClosedRange<T>> C.duration get() = endInclusive - start