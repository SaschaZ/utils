@file:Suppress("unused")

package dev.zieger.utils.time.base

import dev.zieger.utils.time.base.TimeUnit.MS
import dev.zieger.utils.time.base.TimeUnit.N
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.absoluteValue

enum class TimeUnit(
    private val factor: Double,
    private val exponent: Int,
    val shortChar: String,
    val isCloned: Boolean = false
) {

    NANOS(1.0 to -9, "N"),
    N(NANOS),
    MICROS(1.0 to -6, "µ"),
    MC(MICROS),
    MILLI(1.0 to -3, "MS"),
    MS(MILLI),
    SECOND(1.0 to 0, "S"),
    S(SECOND),
    MINUTE(6.0 to 1, "MIN"),
    M(MINUTE),
    HOUR(3.6 to 3, "H"),
    H(HOUR),
    DAY(8.64 to 4, "D"),
    D(DAY),
    WEEK(6.048 to 5, "W"),
    W(WEEK),
    MONTH(2.592 to 6, "M"),
    YEAR(3.1536 to 7, "Y");

    constructor(unit: TimeUnit) : this(unit.factor, unit.exponent, unit.shortChar, true)
    constructor(pair: Pair<Double, Int>, shortChar: String) : this(pair.first, pair.second, shortChar)

    /**
     * Is at least 1.
     */
    val factorNanos = (factor.bigD
            * 10.bigD.pow(9)
            * 10.bigD.pow(exponent.absoluteValue)).let { pow10 ->
        if (exponent >= 0) pow10 / 10.bigD.pow(9) else (10.bigD.pow(9) / pow10)
    }

    override fun toString() =
        "$name(factor=$factor; exponent=$exponent; shortChar=$shortChar; factorNanos=$factorNanos)"
}

fun Pair<TimeUnit, TimeUnit>.convert(value: BigInteger) =
    convert(value, first, second)

fun convert(value: BigInteger, from: TimeUnit, to: TimeUnit) =
    (value.bigD * from.factorNanos / to.factorNanos).bigI

fun BigInteger.toNanos() = toNanos(N)
infix fun BigInteger.toNanos(unit: TimeUnit): BigInteger = (unit to N).convert(this)
fun Number.toNanos() = toNanos(N)
infix fun Number.toNanos(unit: TimeUnit): BigInteger = (unit to N).convert(bigI)
fun BigInteger.toMillis() = toMillis(MS)
infix fun BigInteger.toMillis(unit: TimeUnit): BigInteger = (unit to MS).convert(this)
fun Number.toMillis() = toMillis(MS)
infix fun Number.toMillis(unit: TimeUnit): BigInteger = toMillis(unit)

val Number.bigI: BigInteger get() = toLong().toBigInteger()
val Number.bigD: BigDecimal get() = toDouble().toBigDecimal()
val BigDecimal.bigI: BigInteger get() = toBigInteger()
val BigInteger.bigD: BigDecimal get() = toBigDecimal()