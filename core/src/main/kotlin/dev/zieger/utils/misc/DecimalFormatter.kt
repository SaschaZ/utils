@file:Suppress("unused")

package dev.zieger.utils.misc

import java.math.MathContext
import java.text.DecimalFormat
import java.util.*

fun Number?.format(digits: Int? = null, shortForm: Boolean = false) = this?.let {
    String.format(
        Locale.ENGLISH, "%.${digits ?: decimalPlaces()}f",
        when {
            shortForm && toDouble().abs() > 1000000 -> toDouble() / 1000000.0
            shortForm && toDouble().abs() > 1000 -> toDouble() / 1000.0
            else -> toDouble()
        }
    ).let {
        "$it${
        when {
            shortForm && toDouble().abs() > 1000000 -> "M"
            shortForm && toDouble().abs() > 1000 -> "K"
            else -> ""
        }}"
    }
}

fun Number?.formatExp(digits: Int = 2): String? = if (this == null) null else if (this == 0.0) "0" else
    DecimalFormat("0.${(0 until digits).joinToString("") { "0" }}E0").format(this)

fun Number?.decimalPlaces(): Number? {
    this ?: return null

    val df = DecimalFormat("#.########")
    val formatted = df.format(this)
    var pointIndex = formatted.indexOf(".")
    if (pointIndex < 0)
        pointIndex = formatted.indexOf(",")
    return if (pointIndex < 0) 0 else formatted.lastIndex - pointIndex
}

fun Number?.formatSatDecimal() = negativeToNull()?.format(8)

fun Number.roundToStep(step: Number, decimalPlaces: Int = 8): Number {
    val rem = this % step
    var rounded = this - rem
    if (rem >= step / 2)
        rounded += step
    return rounded.round(decimalPlaces)
}

fun Number.round(decimalPlaces: Int = 8) =
    toDouble().toBigDecimal(MathContext(decimalPlaces)).toDouble() as Number