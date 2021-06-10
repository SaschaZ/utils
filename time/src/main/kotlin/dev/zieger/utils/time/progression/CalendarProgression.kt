package dev.zieger.utils.time.progression

import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.TimeStamp

open class CalendarProgression(
    val start: ITimeStamp,
    val end: () -> ITimeStamp,
    val step: ITimeStamp.() -> ITimeStamp
) : Iterable<ITimeStamp> {
    override fun iterator(): Iterator<ITimeStamp> = object : Iterator<ITimeStamp> {
        private var next: ITimeStamp = start

        override fun hasNext(): Boolean = next <= end()
        override fun next(): ITimeStamp = next.also { next = next.step() }
    }
}

infix fun ClosedRange<ITimeStamp>.step(step: ITimeStamp.() -> ITimeStamp) =
    CalendarProgression(start, { endInclusive }, step)

infix fun ITimeStamp.withStep(step: ITimeStamp.() -> ITimeStamp) =
    CalendarProgression(this, { TimeStamp() }, step)
