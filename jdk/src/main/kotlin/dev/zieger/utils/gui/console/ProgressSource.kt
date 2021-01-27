package dev.zieger.utils.gui.console

import dev.zieger.utils.gui.console.ProgressUnit.Items
import dev.zieger.utils.observable.IObservable
import dev.zieger.utils.observable.Observable
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.minutes
import dev.zieger.utils.time.duration.toDuration
import kotlinx.coroutines.CoroutineScope

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

    val doneSpeed: Double
    val doneSpeedFormatted get() = unit.formatSpeed(doneSpeed)
    val doneFinishedIn: IDurationEx get() = (remaining / doneSpeed).toDuration(TimeUnit.SECOND)

    val unit: ProgressUnit
    val unitsPerSecond: Double
        get() = activeFor.seconds.let { if (it > 0) (done - initial) / it.toDouble() else (done - initial).toDouble() }
    val unitsPerSecondFormatted: String get() = unit.formatSpeed(unitsPerSecond)

    val doneFormatted: String get() = unit.formatAmount(done)
    val totalFormatted: String get() = unit.formatAmount(total)
    val remainingFormatted: String get() = unit.formatAmount(remaining)

    var title: String?
}

class ProgressSource(
    scope: CoroutineScope,
    override val activeSince: ITimeEx = TimeEx(),
    override val initial: Long = 0,
    total: Long = -1,
    override var unit: ProgressUnit = Items(),
    private val doneSpeedDuration: IDurationEx = 1.minutes,
    override var title: String? = null
) : IProgressSource {

    override var lastAction: ITimeEx = TimeEx()

    private var previousDone = HashMap<ITimeEx, Long>()
    override var doneSpeed: Double = 0.0

    override val doneObservable = Observable(initial, scope = scope, safeSet = true) {
        lastAction = TimeEx()
        previousDone[lastAction] = it
        previousDone.filter { (time, _) -> time < lastAction - doneSpeedDuration }
            .forEach { r -> previousDone.remove(r.key) }
        if (previousDone.isNotEmpty())
            doneSpeed = (it - previousDone.values.minOrNull()!!) /
                    (lastAction - previousDone.keys.minOrNull()!!).seconds.toDouble()
    }
    override var done: Long by doneObservable

    override val totalObservable = Observable(total, scope, safeSet = true)
    override var total: Long by totalObservable
}

suspend fun IObservable<Long>.increment() = changeValue { it + 1 }