package dev.zieger.utils.gui.console

import dev.zieger.utils.gui.console.ProgressUnit.Items
import dev.zieger.utils.misc.pretty
import dev.zieger.utils.observable.IObservable
import dev.zieger.utils.observable.Observable
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.toDuration
import kotlinx.coroutines.CoroutineScope

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

interface IProgressSource {

    val activeSince: ITimeEx
    val activeFor: IDurationEx get() = TimeEx() - activeSince
    val finishedIn: IDurationEx get() = (remaining / unitsPerSecond).toDuration(TimeUnit.SECOND)

    val lastAction: ITimeEx
    val lastActionBefore: IDurationEx get() = TimeEx() - lastAction

    val initial: Long

    val doneObservable: Observable<Long>
    var done: Long

    val totalObservable: IObservable<Long>
    var total: Long

    val donePercent: Double get() = done.toDouble() / total
    val remaining: Long get() = total - done

    val unit: ProgressUnit
    val unitsPerSecond: Double get() = (done - initial) / activeFor.seconds.toDouble()
    val unitsPerSecondFormatted: String get() = unit.formatSpeed(unitsPerSecond)

    val doneFormatted: String get() = unit.formatAmount(done)
    val totalFormatted: String get() = unit.formatAmount(total)
    val remainingFormatted: String get() = unit.formatAmount(remaining)

    var subSource: IProgressSource?
}

class ProgressSource(
    scope: CoroutineScope,
    override val activeSince: ITimeEx = TimeEx(),
    override val initial: Long = 0,
    total: Long = -1,
    override var unit: ProgressUnit = Items(),
    override var subSource: IProgressSource? = null
) : IProgressSource {

    override var lastAction: ITimeEx = TimeEx(0L)

    override val doneObservable = Observable(initial, scope, safeSet = true) {
        lastAction = TimeEx()
    }
    override var done: Long by doneObservable

    override val totalObservable = Observable(total, scope, safeSet = true)
    override var total: Long by totalObservable
}