package dev.zieger.utils.gui.console

import dev.zieger.utils.gui.console.ProgressUnit.Items
import dev.zieger.utils.misc.nullWhenBlank
import dev.zieger.utils.observable.IObservable
import dev.zieger.utils.observable.Observable
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

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

    var job: Job?
    var title: String?
    var subSource: IProgressSource?

    fun bind(other: IProgressSource): () -> Unit {
        fun copy() {
            done = other.done
            total = other.total
            job = other.job ?: job
            title = other.title?.nullWhenBlank() ?: title
            subSource = other.subSource ?: subSource
        }
        copy()
        val doneUnObserve = other.doneObservable.observe { copy() }
        val totalUnObserve = other.totalObservable.observe { copy() }
        return { doneUnObserve(); totalUnObserve() }
    }
}

class ProgressSource(
    scope: CoroutineScope,
    override val activeSince: ITimeEx = TimeEx(),
    override val initial: Long = 0,
    total: Long = -1,
    override var unit: ProgressUnit = Items(),
    override var job: Job? = null,
    override var title: String? = null,
    override var subSource: IProgressSource? = null
) : IProgressSource {

    override var lastAction: ITimeEx = TimeEx()

    override val doneObservable = Observable(initial, scope, safeSet = true) {
        lastAction = TimeEx()
    }
    override var done: Long by doneObservable

    override val totalObservable = Observable(total, scope, safeSet = true)
    override var total: Long by totalObservable
}