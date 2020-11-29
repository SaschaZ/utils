package dev.zieger.utils.gui.console

import dev.zieger.utils.gui.console.ProgressUnit.Items
import dev.zieger.utils.misc.format
import dev.zieger.utils.observable.IObservable
import dev.zieger.utils.observable.Observable
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.toDuration
import kotlinx.coroutines.CoroutineScope
import java.text.DecimalFormat

sealed class ProgressUnit {

    abstract fun formatAmount(value: Long): String

    open fun formatSpeed(value: Double): String = "${formatAmount(value.toLong())}/s"

    data class Items(val name: String? = null) : ProgressUnit() {
        override fun formatAmount(value: Long): String =
            "%,d".format(value).let { f -> name?.let { n -> "$f$n" } ?: f }
    }

    object Bytes : ProgressUnit() {
        override fun formatAmount(value: Long): String {
            var newValue = value.toDouble()
            var numDivides = 0
            while (newValue > 1000) {
                newValue /= 1000
                numDivides++
            }
            return "${newValue.format(3)}${
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
        private val amount: (unit: Long) -> String,
        private val speed: (unit: Double) -> String
    ) : ProgressUnit() {
        override fun formatAmount(value: Long): String = amount(value)
        override fun formatSpeed(value: Double): String = speed(value)
    }
}

interface IProgressSource {

    val preText: Any?
    val postText: Any?

    val activeSince: ITimeEx
    val activeFor: IDurationEx get() = TimeEx() - activeSince
    val finishedIn: IDurationEx get() = (remaining / unitsPerSecond).toDuration(TimeUnit.SECOND)
    val lastAction: ITimeEx
    val lastActionBefore: IDurationEx get() = TimeEx() - lastAction

    val doneObservable: IObservable<Long>
    var done: Long
    val doneFormatted: String get() = unit.formatAmount(done)
    val totalObservable: IObservable<Long>
    var total: Long
    val totalFormatted: String get() = unit.formatAmount(total)
    val donePercent: Double get() = done.toDouble() / total
    val remaining: Long get() = total - done
    val remainingFormatted: String get() = unit.formatAmount(remaining)
    val unitsPerSecond: Double get() = done / activeFor.seconds.toDouble()
    val unitsPerSecondFormatted: String get() = unit.formatSpeed(unitsPerSecond)
    val unit: ProgressUnit
}

class ProgressSource(
    scope: CoroutineScope,
    override var preText: Any? = null,
    override var postText: Any? = null,
    override val activeSince: ITimeEx = TimeEx(),
    done: Long = 0,
    total: Long = -1,
    override var unit: ProgressUnit = Items()
) : IProgressSource {

    override var lastAction: ITimeEx = TimeEx(0L)

    override val doneObservable = Observable(done, scope, safeSet = true) {
        lastAction = TimeEx()
    }
    override var done: Long by doneObservable

    override val totalObservable = Observable(total, scope, safeSet = true)
    override var total: Long by totalObservable
}