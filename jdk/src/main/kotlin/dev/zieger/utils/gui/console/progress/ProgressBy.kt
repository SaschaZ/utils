@file:Suppress("FunctionName")

package dev.zieger.utils.gui.console.progress

import dev.zieger.utils.gui.console.*
import dev.zieger.utils.gui.console.progress.ProgressEntity.*
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.minutes
import kotlinx.coroutines.CoroutineScope

suspend fun <R> CoroutineScope.progressBy(
    title: String = "",
    initial: Long = 0,
    total: Long = 0,
    unit: ProgressUnit = ProgressUnit.Items(),
    doneSpeedDuration: IDurationEx = 1.minutes,
    doNotRemove: Boolean = false,
    consoleIdx: Int = 0,
    consoleScope: IConsoleScope = GlobalConsoleScope,
    removeWhen: Double = 1.0,
    vararg entities: ProgressEntity = DEFAULT_PROGRESS_ENTITIES(removeWhen, title),
    block: suspend IProgressSource.() -> R
): R = ProgressSource(
    this,
    title = title,
    initial = initial,
    total = total,
    unit = unit,
    doneSpeedDuration = doneSpeedDuration
).run {
    val removeProg = consoleScope.outnl(consoleIdx, *PROGRESS(this@run, *entities))
    block().apply { if (!doNotRemove) removeProg() }
}

fun DEFAULT_PROGRESS_ENTITIES(label: (IProgressTextScope.() -> String)? = null) =
    DEFAULT_PROGRESS_ENTITIES(1.0, label)

fun DEFAULT_PROGRESS_ENTITIES(removeWhen: Double = 1.0, label: String) =
    DEFAULT_PROGRESS_ENTITIES(removeWhen) { label }

fun DEFAULT_PROGRESS_ENTITIES(removeWhen: Double = 1.0, label: (IProgressTextScope.() -> String)? = null) =
    arrayOf(
        Text { label?.invoke(this@Text) ?: title ?: "" },
        Space, Bar(), Space, DoneSpeed(), Space, DoneOfTotal(), RemoveWhen(removeWhen)
    )

