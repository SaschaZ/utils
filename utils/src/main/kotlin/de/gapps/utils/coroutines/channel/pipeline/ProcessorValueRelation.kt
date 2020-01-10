package de.gapps.utils.coroutines.channel.pipeline

/**
 * Defines relation between input and output amount.
 */
sealed class ProcessorValueRelation(
    val input: Int,
    val output: Int
) {
    companion object {
        const val N = Int.MAX_VALUE // unspecified but more than 1
    }

    object OneToOne : ProcessorValueRelation(1, 1) // same amount out as in
    class OneToN(n: Int = N) : ProcessorValueRelation(1, n) // n times output amount as in
    class NToOne(n: Int = N) : ProcessorValueRelation(n, 1) // and so on...
    open class NToN(n0: Int = N, n1: Int = N) : ProcessorValueRelation(n0, n1) {
        constructor(pair: Pair<Int, Int>) : this(pair.first, pair.second)
    }

    object Unspecified : NToN()

    override fun toString() = "(${if (input == N) "N" else "$input"} to ${if (output == N) "N" else "$output"})"
}