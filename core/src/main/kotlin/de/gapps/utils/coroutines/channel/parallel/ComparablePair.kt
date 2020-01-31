package de.gapps.utils.coroutines.channel.parallel

data class ComparablePair<T : Comparable<T>, U : Any>(val first: T, val second: U) : Comparable<ComparablePair<T, U>> {
    override fun compareTo(other: ComparablePair<T, U>) = first.compareTo(other.first)
}