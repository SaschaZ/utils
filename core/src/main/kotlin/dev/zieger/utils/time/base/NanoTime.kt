@file:Suppress("unused", "ClassName")

package dev.zieger.utils.time.base

import dev.zieger.utils.misc.castSafe
import dev.zieger.utils.misc.toBigInteger
import dev.zieger.utils.time.base.INanoTime.Companion.BaseType
import dev.zieger.utils.time.base.TimeUnit.*
import dev.zieger.utils.time.duration.nanos
import dev.zieger.utils.time.duration.years
import java.math.BigInteger

interface INanoTime : Comparable<INanoTime>, Comparator<INanoTime> {

    companion object {

        enum class BaseType {
            SECONDS,
            MILLIS,
            NANOS
        }
    }

    var secondsInternal: Long
    var millisInternal: Long
    var nanosInternal: BigInteger

    var baseType: BaseType

    var base: Number
        get() = when (baseType) {
            BaseType.SECONDS -> secondsInternal
            BaseType.MILLIS -> millisInternal
            BaseType.NANOS -> nanosInternal
        }
        set(value) = when (baseType) {
            BaseType.SECONDS -> secondsInternal = value.toLong()
            BaseType.MILLIS -> millisInternal = value.toLong()
            BaseType.NANOS -> nanosInternal = value.toBigInteger()
        }

    override fun compareTo(other: INanoTime): Int = nanosInternal.compareTo(other.nanosInternal)
    override fun compare(p0: INanoTime, p1: INanoTime) = p0.nanosInternal.compareTo(p1.nanosInternal)

    val micros: Number
        get() = (N to MC).convert(base)
    val millis: Number
        get() = (N to MS).convert(base)
    val seconds: Number
        get() = (N to S).convert(base)
    val minutes: Number
        get() = (N to M).convert(base)
    val hours: Number
        get() = (N to H).convert(base)
    val days: Number
        get() = (N to D).convert(base)
    val weeks: Number
        get() = (N to W).convert(base)
    val months: Number
        get() = (N to MONTH).convert(base)
    val years: Number
        get() = (N to YEAR).convert(base)

    val notZero: Boolean
        get() = base != 0.toBigInteger()

    val positive: Boolean
        get() = base > 0

    val negative: Boolean
        get() = base < 0
}

open class NanoTime(
    override var baseType: BaseType = BaseType.MILLIS,
    override var nanosInternal: BigInteger = System.nanoTime().bigI,
    override var millisInternal: Long = System.currentTimeMillis(),
    override var secondsInternal: Long = millisInternal / 1000
) : INanoTime {

    companion object {

        private const val tsStart = 1970L
        private val Number.leapYears get() = this / 4 - this / 100 + this / 400
        private val to1970: BigInteger = tsStart.years.nanos + tsStart.leapYears.days
        val Pair<Long, Long>.big: BigInteger
            get() = first.bigI.add(to1970).toNanos(MS) + (second % 1000000).bigI
    }

    override fun equals(other: Any?): Boolean = other?.castSafe<INanoTime>()?.let {
        base == it.base
    } == true

    override fun hashCode(): Int = base.hashCode()
}

operator fun Number.compareTo(other: INanoTime): Int = bigI.compareTo(other.nanosInternal)
operator fun Number.compareTo(other: Number): Int = toLong().compareTo(other.toLong())

fun <T : INanoTime> min(a: T, b: T): T = if (a < b) a else b
fun <T : INanoTime> max(a: T, b: T): T = if (a > b) a else b

fun <T : INanoTime> List<T>.oldest(): T? = minBy { it.nanosInternal }
fun <T : INanoTime> List<T>.latest(): T? = maxBy { it.nanosInternal }
fun <T : INanoTime> List<T>.sort(): List<T> = sortedBy { it.nanosInternal }
fun <T : INanoTime> List<T>.sortDesc(): List<T> = sortedByDescending { it.nanosInternal }