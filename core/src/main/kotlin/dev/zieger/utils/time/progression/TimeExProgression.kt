package dev.zieger.utils.time.progression

import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.base.bigI
import dev.zieger.utils.time.base.plus
import dev.zieger.utils.time.base.times
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.toTime

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
    (this..(other.millis - 1.bigI).toTime())

infix fun ClosedRange<ITimeEx>.step(step: Number) =
    TimeExProgression(start, endInclusive, step.milliseconds)

infix fun ClosedRange<ITimeEx>.step(step: IDurationEx) =
    TimeExProgression(start, endInclusive, step)