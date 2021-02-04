@file:Suppress("FunctionName")

package dev.zieger.utils.gui.console.progress

import dev.zieger.utils.gui.console.*
import dev.zieger.utils.gui.console.progress.ProgressEntity.*
import dev.zieger.utils.misc.nullWhenBlank
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.minutes
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.coroutineContext

interface IProgressByScope : IProgressSource {
    fun removeProgress()
}

class ProgressByScope(
    progressSource: ProgressSource,
    private val remove: () -> Unit
) : IProgressByScope,
    IProgressSource by progressSource {

    override fun removeProgress() = remove()
}

suspend fun <R> progressBy(
    title: String = "",
    initial: Long = 0,
    total: Long = 0,
    unit: ProgressUnit = ProgressUnit.Items(),
    doneSpeedDuration: IDurationEx = 1.minutes,
    doNotRemove: Boolean = false,
    consoleIdx: Int = 0,
    consoleScope: IConsoleScope = GlobalConsoleScope,
    showSpeed: Boolean = true,
    removeWhen: Double = 1.0,
    vararg entities: ProgressEntity = DEFAULT_PROGRESS_ENTITIES(showSpeed, { donePercent >= removeWhen }, { title }),
    block: suspend IProgressByScope.() -> R
): R = CoroutineScope(coroutineContext).progressBy(
    title, initial, total, unit, doneSpeedDuration, doNotRemove,
    consoleIdx, consoleScope, showSpeed, removeWhen, *entities, block = block
)

suspend fun <R> CoroutineScope.progressBy(
    title: String = "",
    initial: Long = 0,
    total: Long = 0,
    unit: ProgressUnit = ProgressUnit.Items(),
    doneSpeedDuration: IDurationEx = 1.minutes,
    doNotRemove: Boolean = false,
    consoleIdx: Int = 0,
    consoleScope: IConsoleScope = GlobalConsoleScope,
    showSpeed: Boolean = true,
    removeWhen: Double = 1.0,
    vararg entities: ProgressEntity = DEFAULT_PROGRESS_ENTITIES(showSpeed, { donePercent >= removeWhen }, { title }),
    block: suspend IProgressByScope.() -> R
): R = ProgressSource(
    this,
    title = title,
    initial = initial,
    total = total,
    unit = unit,
    doneSpeedDuration = doneSpeedDuration
).let {
    var removeProg: () -> Unit = {}
    ProgressByScope(it) { removeProg() }.run {
        removeProg = consoleScope.outnl(consoleIdx, *PROGRESS(this@run, *entities))
        block().apply { if (!doNotRemove) removeProg() }
    }
}

fun DEFAULT_PROGRESS_ENTITIES(title: String): Array<ProgressEntity> =
    DEFAULT_PROGRESS_ENTITIES { title }

fun DEFAULT_PROGRESS_ENTITIES(
    showSpeed: Boolean = true,
    removeWhen: (IProgressSource.() -> Boolean)? = null,
    title: (IProgressTextScope.() -> String)? = null
): Array<ProgressEntity> = arrayOf(
    Text { this.title?.nullWhenBlank() ?: title?.invoke(this) ?: "" },
    Space, Bar(), Space, if (showSpeed) DoneSpeed() else null, Space,
    DoneOfTotal(), removeWhen?.let { RemoveWhen(it) }
).filterNotNull().toTypedArray()

