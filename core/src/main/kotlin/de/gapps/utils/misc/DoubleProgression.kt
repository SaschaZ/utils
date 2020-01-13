package de.gapps.utils.misc

infix fun ClosedRange<Double>.step(step: Double) = DoubleProgression(start, endInclusive, step)

data class DoubleProgression(
    val start: Double,
    val end: Double,
    val step: Double
) : Iterable<Double> {
    init {
        if (step == 0.0) throw IllegalArgumentException("Step must be non-zero.")
        if (step == Double.MIN_VALUE) throw IllegalArgumentException("Step must be greater than Double.MIN_VALUE to avoid overflow on negation.")
    }

    override fun iterator(): DoubleIterator = object : DoubleIterator() {
        private var value: Double? = start

        override fun hasNext() = value != null

        override fun nextDouble(): Double {
            val result = value ?: throw IllegalStateException("Called nextDouble() for empty Iterable")
            value = result + step
            return result
        }
    }

    fun isEmpty(): Boolean = if (step > 0) start > end else start < end
}