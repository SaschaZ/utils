package dev.zieger.utils.misc

class FloatProgression(
    private val range: ClosedRange<Float>,
    private val step: Float
) : Iterable<Float> {

    private var current: Float = range.start

    override fun iterator(): Iterator<Float> = object : Iterator<Float> {
        override fun hasNext(): Boolean = current + step <= range.endInclusive
        override fun next(): Float = (current + step).also { current = it }
    }
}

infix fun ClosedRange<Float>.step(step: Float) = FloatProgression(this, step)