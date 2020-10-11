package dev.zieger.utils.misc

import kotlin.experimental.and

private val Int.singleHex: String
    get() = when (toInt()) {
        0 - 9 -> "${toInt()}"
        10 -> "A"
        11 -> "B"
        12 -> "C"
        13 -> "D"
        14 -> "E"
        15 -> "F"
        else -> "X"
    }
val Byte.hex: String get() = (toInt() and 0xF).singleHex + ((toInt() and 0xF0) shr 4).singleHex

val Long.hex: String
    get() = (this shr 24 and 0xFF).toByte().hex +
            (this shr 16 and 0xFF).toByte().hex +
            (this shr 8 and 0xFF).toByte().hex +
            (this and 0xFF).toByte().hex