package dev.zieger.utils.time.base

import dev.zieger.utils.misc.pow
import dev.zieger.utils.time.base.TimeUnit.MS
import kotlin.math.absoluteValue

enum class TimeUnit(
    private val factor: Double,
    private val exponent: Int,
    val shortChar: String,
    val isCloned: Boolean = false
) {

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
    val factorMillis = (1000 * factor * 10.pow(exponent.absoluteValue).let { pow10 ->
        if (exponent >= 0) pow10 else (1 / pow10)
    }).toLong()

    override fun toString() =
        "$name(factor=$factor; exponent=$exponent; shortChar=$shortChar; facExpProduct=$factorMillis)"
}

fun Pair<TimeUnit, TimeUnit>.convert(value: Long) =
    (value * first.factorMillis / second.factorMillis.toDouble()).toLong()

fun Long.toMillis(unit: TimeUnit = MS): Long = (unit to MS).convert(this)

val String.timeUnit
    get() = when (this) {
        "s" -> TimeUnit.SECOND
        "m" -> TimeUnit.MINUTE
        "h" -> TimeUnit.HOUR
        "d" -> TimeUnit.DAY
        "w" -> TimeUnit.WEEK
        "M" -> TimeUnit.MONTH
        "y" -> TimeUnit.YEAR
        else -> throw IllegalArgumentException("Can not convert $this to a TimeUnit")
    }