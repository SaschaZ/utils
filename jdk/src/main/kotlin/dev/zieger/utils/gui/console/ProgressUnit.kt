package dev.zieger.utils.gui.console

import dev.zieger.utils.misc.pretty
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.duration.toDuration

sealed class ProgressUnit {

    abstract fun formatAmount(value: Number): String

    open fun formatSpeed(value: Number): String = when {
        value.toDouble() == 0.0 -> ""
        value.toDouble() < 1.0 -> "1/${(1 / value.toDouble()).toInt().toDuration(TimeUnit.SECOND)}"
        else -> "${formatAmount(value)}/s"
    }

    data class Items(val name: String? = null) : ProgressUnit() {
        override fun formatAmount(value: Number): String =
            value.pretty.let { f -> name?.let { n -> "$f$n" } ?: f }
    }

    object Bytes : ProgressUnit() {
        override fun formatAmount(value: Number): String {
            var newValue = value.toDouble()
            var numDivides = 0
            do {
                newValue /= 1000
                numDivides++
            } while (newValue > 1000)
            return "${newValue.pretty}${
                when (numDivides) {
                    0 -> "B"
                    1 -> "KB"
                    2 -> "MB"
                    3 -> "GB"
                    4 -> "TB"
                    5 -> "PB"
                    else -> "[10^$numDivides]"
                }
            }"
        }
    }

    class Custom(
        private val amount: (unit: Number) -> String,
        private val speed: (unit: Number) -> String
    ) : ProgressUnit() {
        override fun formatAmount(value: Number): String = amount(value)
        override fun formatSpeed(value: Number): String = speed(value)
    }
}