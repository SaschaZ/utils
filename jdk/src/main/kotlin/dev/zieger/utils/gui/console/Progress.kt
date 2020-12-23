@file:Suppress("unused", "FunctionName")

package dev.zieger.utils.gui.console

import dev.zieger.utils.gui.console.TextWithColor.Companion.newGroupId

@Suppress("FunctionName")
fun PROGRESS(
    progressSource: IProgressSource,
    vararg entities: ProgressEntity
): List<TextWithColor> {
    val groupId = newGroupId
    return entities.flatMap { it.textWithColor(progressSource) }.map { it.copy(groupId = groupId) }
}

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
    fun MessageScope.removeWhenComplete() {
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