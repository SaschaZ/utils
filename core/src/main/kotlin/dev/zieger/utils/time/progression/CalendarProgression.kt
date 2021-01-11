package dev.zieger.utils.time.progression

import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx

open class CalendarProgression(
    val start: ITimeEx,
    val end: () -> ITimeEx,
    val step: ITimeEx.() -> ITimeEx
) : Iterable<ITimeEx> {
    override fun iterator(): Iterator<ITimeEx> = object : Iterator<ITimeEx> {
        private var next: ITimeEx = start

        override fun hasNext(): Boolean = next <= end()
        override fun next(): ITimeEx = next.also { next = next.step() }
    }
}

infix fun ClosedRange<ITimeEx>.step(step: ITimeEx.() -> ITimeEx) =
    CalendarProgression(start, { endInclusive }, step)

infix fun ITimeEx.withStep(step: ITimeEx.() -> ITimeEx) =
    CalendarProgression(this, { TimeEx() }, step)
