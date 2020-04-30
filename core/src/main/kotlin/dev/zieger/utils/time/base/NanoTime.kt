package dev.zieger.utils.time.base

import dev.zieger.utils.misc.castSafe

interface INanoTime {

    val millis: Long
    val nanos: Long
}

open class NanoTime(
    override var millis: Long = 0L,
    override var nanos: Long = 0L
) : INanoTime {

    operator fun plus(other: INanoTime): INanoTime {
        val result = NanoTime(millis + other.millis, nanos + other.nanos)
        val hasOverflow = result.nanos - nanos != other.nanos
        if (hasOverflow)
            result.millis += when {
                other.nanos >= 0 -> 1
                else -> -1
            }
        return result
    }

    operator fun plusAssign(other: INanoTime) {
        millis += other.millis
        val oldNanos = nanos
        nanos += other.nanos
        val hasOverflow = nanos - oldNanos != other.nanos
        if (hasOverflow)
            millis += when {
                other.nanos >= 0 -> 1
                else -> -1
            }
    }

    operator fun minus(other: INanoTime): INanoTime =
        (nanos - other.nanos).let { NanoTime(millis - other.millis + overflow(other, it), it) }

    operator fun times(other: INanoTime): INanoTime =
        (nanos * other.nanos).let { NanoTime(millis * other.millis + overflow(other, it), it) }

    operator fun div(other: INanoTime): INanoTime =
        (nanos / other.nanos).let { NanoTime(millis / other.millis + overflow(other, it), it) }

    private fun overflow(other: INanoTime, newNanos: Long): Long = when {
        nanos - newNanos != other.nanos -> when {
            other.nanos >= 0 -> 1
            else -> -1
        }
        else -> 0
    }

    override fun equals(other: Any?): Boolean = other?.castSafe<INanoTime>()?.let {
        millis == it.millis && nanos == it.nanos
    } == true

    override fun hashCode(): Int = millis.hashCode() + nanos.hashCode()
}