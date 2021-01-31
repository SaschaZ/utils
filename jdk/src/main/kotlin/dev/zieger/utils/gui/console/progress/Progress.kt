@file:Suppress("unused", "FunctionName")

package dev.zieger.utils.gui.console.progress

import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.gui.console.*
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.minutes
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun PROGRESS(
    vararg entities: ProgressEntity,
    scope: CoroutineScope = IoCoroutineScope(),
    activeSince: ITimeEx = TimeEx(),
    initial: Long = 0,
    total: Long = -1,
    unit: ProgressUnit = ProgressUnit.Items(),
    doneSpeedDuration: IDurationEx = 1.minutes,
    title: String? = null
): Array<TextWithColor> = PROGRESS(
    ProgressSource(scope, activeSince, initial, total, unit, doneSpeedDuration, title), *entities
)

@Suppress("FunctionName")
fun PROGRESS(
    progressSource: IProgressSource,
    vararg entities: ProgressEntity
): Array<TextWithColor> = entities.map { it.textWithColor(progressSource) }.toTypedArray()

@Deprecated("")
@Suppress("FunctionName")
fun PROGRESS(
    progressSource: IProgressSource,
    removeWhenComplete: Boolean = true,
    initial: Int = 0,
    max: Int = 100,
    size: Int = ConsoleProgressBar.DEFAULT_SIZE,
    foreground: IProgressColorProvider = ConsoleProgressBar.PROGRESS_COLORS,
    background: IProgressColorProvider = ColorGradient(0x000000),
    fraction: String = ConsoleProgressBar.FRACTION_2,
    preText: Any? = null,
    postText: Any? = null,
    preTextProvider: ((progress: Double) -> Any?)? = { preText },
    postTextProvider: ((progress: Double) -> Any?)? = { postText }
): Array<TextWithColor> {
    fun ITextScope.removeWhenComplete() {
        if (removeWhenComplete && progressSource.donePercent >= 0.999) remove()
    }

    fun preText(): TextWithColor = text {
        removeWhenComplete()
        preTextProvider?.invoke(progressSource.donePercent)?.toString() ?: ""
    }

    val hideBar = max == -1
    fun bar(): TextWithColor? =
        if (hideBar) null else ConsoleProgressBar(progressSource, size, foreground, background, fraction)
            .textWithColor { removeWhenComplete() }

    fun afterBarText(): TextWithColor = text {
        removeWhenComplete()
        " ${progressSource.unitsPerSecondFormatted}${if (hideBar) " - " else "\n"}" +
                progressSource.doneFormatted +
                if (progressSource.total > 0) "|${progressSource.totalFormatted} - " else " - " +
                        "${progressSource.activeFor.formatDuration(maxEntities = 2, sameLength = true)} - " +
                        (if (progressSource.total > 0) progressSource.finishedIn.formatDuration(
                            maxEntities = 2,
                            sameLength = true
                        ).let { "$it - " } else "") +
                        progressSource.lastActionBefore.formatDuration(maxEntities = 2, sameLength = true)
    }

    fun postText(): TextWithColor = text {
        removeWhenComplete()
        postTextProvider?.invoke(progressSource.donePercent)?.toString() ?: ""
    }

    return listOfNotNull(preText(), bar(), afterBarText(), postText()).toTypedArray()
}