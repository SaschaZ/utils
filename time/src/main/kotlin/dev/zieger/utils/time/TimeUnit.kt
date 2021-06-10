package dev.zieger.utils.time

import dev.zieger.utils.time.TimeUnit.MS
import kotlin.math.absoluteValue

//@Serializable
enum class TimeUnit(
    private val factor: Double,
    private val exponent: Int,
    val shortChar: String,
    val isCloned: Boolean = false
) {

    MILLI(1.0 to -3, "m"),
    MS(MILLI),
    SECOND(1.0 to 0, "s"),
    S(SECOND),
    MINUTE(6.0 to 1, "M"),
    M(MINUTE),
    HOUR(3.6 to 3, "H"),
    H(HOUR),
    DAY(8.64 to 4, "D"),
    D(DAY),
    WEEK(6.048 to 5, "W"),
    W(WEEK),
    MONTH(2.592 to 6, "Ⅿ"),
    YEAR(3.1536 to 7, "Y");

    constructor(unit: TimeUnit) : this(unit.factor, unit.exponent, unit.shortChar, true)
    constructor(pair: Pair<Double, Int>, shortChar: String) : this(pair.first, pair.second, shortChar)

    /**
     * Is at least 1.
     */
    val factorMillis = (1000 * factor * Math.pow(10.0, exponent.absoluteValue.toDouble()).let { pow10 ->
        if (exponent >= 0) pow10 else (1 / pow10)
    }).toLong()

    override fun toString() =
        "$name(factor=$factor; exponent=$exponent; shortChar=$shortChar; facExpProduct=$factorMillis)"
}

fun Number.convert(fromTo: Pair<TimeUnit, TimeUnit>): Long =
    (toDouble() * fromTo.first.factorMillis / fromTo.second.factorMillis.toDouble()).toLong()

fun Long.toMillis(unit: TimeUnit = MS): Long = convert(unit to MS)

val String.timeUnit: TimeUnit?
    get() = TimeUnit.values().firstOrNull { it.shortChar == this }