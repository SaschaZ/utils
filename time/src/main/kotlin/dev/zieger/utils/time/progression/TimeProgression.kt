package dev.zieger.utils.time.progression

import dev.zieger.utils.time.ITimeSpan
import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.millis
import dev.zieger.utils.time.toTime

open class TimeProgression(
    val start: ITimeStamp,
    val end: ITimeStamp,
    val step: ITimeSpan
) : Iterable<ITimeStamp> {

    override fun iterator(): Iterator<ITimeStamp> = object : Iterator<ITimeStamp> {
        private var index: Int = 0

        override fun hasNext() = start + step * (index + 1) <= end

        override fun next(): ITimeStamp = start + step * ++index
    }
}

infix fun <A: ITimeStamp, B: ITimeStamp> A.until(other: B) =
    (this..(other.millis - 1).toTime())

infix fun <T: ITimeStamp> ClosedRange<T>.step(step: Number) =
    TimeProgression(start, endInclusive, step.millis)

infix fun <T: ITimeStamp> ClosedRange<T>.step(step: ITimeSpan) =
    TimeProgression(start, endInclusive, step)

fun ClosedRange<ITimeStamp>.ticks(
    interval: ITimeSpan,
    count: Int = 1
): TimeProgression = this step (interval * count).timeSpan

fun ClosedRange<ITimeSpan>.ticks(
    interval: ITimeSpan,
    count: Int = 1
): DurationExProgression = this step (interval * count).timeSpan

val ClosedRange<ITimeStamp>.duration get() = endInclusive - start