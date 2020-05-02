@file:Suppress("unused")

package dev.zieger.utils.time.base

import dev.zieger.utils.misc.castSafe
import java.math.BigInteger

interface INanoTime : Comparable<INanoTime>, Comparator<INanoTime> {

    var nanos: BigInteger

    operator fun plusAssign(other: INanoTime) {
        nanos += other.nanos
    }

    operator fun plusAssign(other: Number) {
        nanos += other.bigI
    }

    operator fun minusAssign(other: INanoTime) {
        nanos -= other.nanos
    }

    operator fun minusAssign(other: Number) {
        nanos -= other.bigI
    }

    operator fun timesAssign(other: INanoTime) {
        nanos *= other.nanos
    }

    operator fun timesAssign(other: Number) {
        nanos *= other.bigI
    }

    operator fun divAssign(other: INanoTime) {
        nanos /= other.nanos
    }

    operator fun divAssign(other: Number) {
        nanos /= other.bigI
    }

    operator fun remAssign(other: INanoTime) {
        nanos %= other.nanos
    }

    operator fun remAssign(other: Number) {
        nanos %= other.bigI
    }

    override fun compareTo(other: INanoTime): Int = nanos.compareTo(other.nanos)
    override fun compare(p0: INanoTime, p1: INanoTime) = p0.nanos.compareTo(p1.nanos)

    val micros: BigInteger
        get() = (TimeUnit.N to TimeUnit.MC).convert(nanos)
    val millis: BigInteger
        get() = (TimeUnit.N to TimeUnit.MS).convert(nanos)
    val seconds: BigInteger
        get() = (TimeUnit.N to TimeUnit.S).convert(nanos)
    val minutes: BigInteger
        get() = (TimeUnit.N to TimeUnit.M).convert(nanos)
    val hours: BigInteger
        get() = (TimeUnit.N to TimeUnit.H).convert(nanos)
    val days: BigInteger
        get() = (TimeUnit.N to TimeUnit.D).convert(nanos)
    val weeks: BigInteger
        get() = (TimeUnit.N to TimeUnit.W).convert(nanos)
    val months: BigInteger
        get() = (TimeUnit.N to TimeUnit.MONTH).convert(nanos)
    val years: BigInteger
        get() = (TimeUnit.N to TimeUnit.YEAR).convert(nanos)

    val notZero: Boolean
        get() = nanos != 0.toBigInteger()

    val positive: Boolean
        get() = nanos > 0.toBigInteger()

    val negative: Boolean
        get() = nanos < 0.toBigInteger()
}

open class NanoTime(
    override var nanos: BigInteger = System.nanoTime().toBigInteger()
) : INanoTime {

    companion object {

        val Pair<Long, Long>.big get() = first.bigI.shiftLeft(Long.SIZE_BITS) + second.bigI
    }

    override fun equals(other: Any?): Boolean = other?.castSafe<INanoTime>()?.let {
        nanos == it.nanos
    } == true

    override fun hashCode(): Int = nanos.hashCode()
}

operator fun Number.compareTo(other: INanoTime) = bigI.compareTo(other.nanos)

fun <T : INanoTime> min(a: T, b: T): T = if (a < b) a else b
fun <T : INanoTime> max(a: T, b: T): T = if (a > b) a else b

fun <T : INanoTime> List<T>.oldest(): T? = minBy { it.nanos }
fun <T : INanoTime> List<T>.latest(): T? = maxBy { it.nanos }
fun <T : INanoTime> List<T>.sort(): List<T> = sortedBy { it.nanos }
fun <T : INanoTime> List<T>.sortDesc(): List<T> = sortedByDescending { it.nanos }