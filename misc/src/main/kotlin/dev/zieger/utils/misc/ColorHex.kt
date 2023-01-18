package dev.zieger.utils.misc

import java.awt.Color
import kotlin.math.pow

fun String.parseArgb(): Color? =
    removePrefix("0x")
        .removePrefix("#")
        .split(2)
        .run {
            when (size) {
                4 -> Color(get(1).hexToFloat, get(2).hexToFloat, get(3).hexToFloat, get(0).hexToFloat)
                3 -> Color(get(0).hexToFloat, get(1).hexToFloat, get(2).hexToFloat)
                else -> null
            }
        }

fun Color.formatArgb(): String = "0x" + alpha.hex + red.hex + green.hex + blue.hex

val String.hexToFloat: Float
    get() {
        return ((getOrNull(0)?.hexToInt ?: 0) +
                (getOrNull(1)?.hexToInt?.let { it * 2.0.pow(4) } ?: 0.0))
            .div(2.0.pow(8)).toFloat().coerceIn(0f..1f)
    }

private val Char.hexToInt: Int
    get() = when (this) {
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9' -> "$this".toInt()
        'A' -> 10
        'B' -> 11
        'C' -> 12
        'D' -> 13
        'E' -> 14
        else -> 15
    }

private fun String.split(everyCharacters: Int): Array<String> {
    var idx = 0
    return groupBy { idx++ / everyCharacters }.values.map { it.toString() }.toTypedArray()
}

private val Float.hex: String
    get() {
        val rawInt = (coerceIn(0f..1f) * 2.0.pow(8)).toInt()
        val lower = rawInt and 0xF
        val upper = (rawInt and 0xF0).shr(4)
        return upper.hexChar + lower.hexChar
    }

private val Int.hex: String
    get() {
        val lower = this and 0xF
        val upper = (this and 0xF0).shr(4)
        return upper.hexChar + lower.hexChar
    }

private val Int.hexChar: String
    get() = when {
        this < 10 -> "$this"
        this == 10 -> "A"
        this == 11 -> "B"
        this == 12 -> "C"
        this == 13 -> "D"
        this == 14 -> "E"
        else -> "F"
    }