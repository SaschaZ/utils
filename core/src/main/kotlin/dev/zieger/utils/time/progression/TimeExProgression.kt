package dev.zieger.utils.time.progression

import dev.zieger.utils.time.*
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx

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

infix fun <A: ITimeEx, B: ITimeEx> A.until(other: B) =
    (this..(other.millis - 1).toTime())

infix fun <T: ITimeEx> ClosedRange<T>.step(step: Number) =
    TimeExProgression(start, endInclusive, step.milliseconds)

infix fun <T: ITimeEx> ClosedRange<T>.step(step: IDurationEx) =
    TimeExProgression(start, endInclusive, step)

fun ClosedRange<ITimeEx>.ticks(
    interval: IDurationEx,
    count: Int = 1
): TimeExProgression = start..endInclusive step interval * count

fun ClosedRange<IDurationEx>.ticks(
    interval: IDurationEx,
    count: Int = 1
): DurationExProgression = start..endInclusive step interval * count

fun ITimeEx.rangeBase(base: IDurationEx): ITimeEx { return this - this % base }

fun ITimeEx.toRange(rangeStep: IDurationEx): ClosedRange<ITimeEx> = rangeBase(rangeStep).let { it .. it + rangeStep }