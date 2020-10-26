package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import dev.zieger.utils.gui.console.*
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


interface IProgressSource {

    val name: String?

    val activeSince: ITimeEx
    val activeFor: IDurationEx get() = TimeEx() - activeSince
    val finishedIn: IDurationEx get() = ((totalBytes - doneBytes) / 1024.0 / 1024 / mbPerSecond).toDuration(TimeUnit.SECOND)

    val doneBytesObservable: IObservable<Long>
    var doneBytes: Long
    val totalBytesObservable: IObservable<Long>
    var totalBytes: Long
    val doneBytesPercent: Double get() = doneBytes.toDouble() / totalBytes
    val mbPerSecond: Double get() = (doneBytes / 1024 / 1024) / activeFor.seconds.toDouble()

    val doneItemsObservable: IObservable<Long>
    var doneItems: Long
    val totalItemsObservable: IObservable<Long>
    var totalItems: Long
    val doneItemsPercent: Double get() = doneItems.toDouble() / totalItems
    val itemsPerSecond: Double get() = doneItems / activeFor.seconds.toDouble()
}

class ProgressSource(scope: CoroutineScope, override val name: String? = null) : IProgressSource {

    override val activeSince: ITimeEx = TimeEx()

    override val doneBytesObservable = Observable(0L, scope, safeSet = true)
    override var doneBytes: Long by doneBytesObservable
    override val totalBytesObservable = Observable(-1L, scope, safeSet = true)
    override var totalBytes: Long by totalBytesObservable
    override val doneItemsObservable = Observable(0L, scope, safeSet = true)
    override var doneItems: Long by doneItemsObservable
    override val totalItemsObservable = Observable(-1L, scope, safeSet = true)
    override var totalItems: Long by totalItemsObservable
}