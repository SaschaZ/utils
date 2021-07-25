package dev.zieger.utils.misc

class DoubleProgression(
    private val range: ClosedRange<Double>,
    private val step: Double
) : Iterable<Double> {

    private var current: Double = range.start

    override fun iterator(): Iterator<Double> = object : Iterator<Double> {
        override fun hasNext(): Boolean = current + step <= range.endInclusive
        override fun next(): Double = (current + step).also { current = it }
    }
}

infix fun ClosedRange<Double>.step(step: Double) = DoubleProgression(this, step)