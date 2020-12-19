@file:Suppress("unused", "FunctionName")

package dev.zieger.utils.gui.console

import dev.zieger.utils.gui.console.TextWithColor.Companion.newGroupId
import dev.zieger.utils.time.duration.IDurationEx

sealed class ProgressEntity {

    abstract fun textWithColor(source: IProgressSource): List<TextWithColor>

    data class Bar(
        private val size: Int = ConsoleProgressBar.DEFAULT_SIZE,
        private val foreground: IProgressColorProvider = ConsoleProgressBar.PROGRESS_COLORS,
        private val background: IProgressColorProvider = ColorGradient(0x000000),
        private val fraction: String = ConsoleProgressBar.FRACTION_2,
        private val midPart: (done: Long, max: Long) -> String? = { d, m ->
            (d / m.toDouble() * 100).let { " %.1f%% ".format(it) }
        }
    ) : ProgressEntity() {

        private val IProgressSource.bar
            get() = ConsoleProgressBar(
                this, size = size, foreground = foreground, background = background,
                fraction = fraction, midPart = midPart
            )

        override fun textWithColor(source: IProgressSource): List<TextWithColor> = listOf(source.bar.textWithColor)
    }

    open class Text(private val text: IProgressSource.() -> List<TextWithColor>) : ProgressEntity() {
        constructor(text: String, color: MessageColor? = null, background: MessageColor? = null) :
                this({ listOf(text({ text }, color, background)) })

        override fun textWithColor(source: IProgressSource): List<TextWithColor> = source.text()
    }

    open class ItemsPerSecond(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(text({ unitsPerSecondFormatted }, color, background)) })

    open class FinishedIn(maxEntities: Int = 2, color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(text({ finishedIn.formatDuration(maxEntities = maxEntities) }, color, background)) })

    open class LastAction(
        minDuration: IDurationEx? = null,
        color: MessageColor? = null, background: MessageColor? = null
    ) : Text({
        listOf(text({
            if (minDuration?.let { it <= lastActionBefore } != false) lastActionBefore else ""
        }, color, background))
    })

    open class Remaining(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(text({ remainingFormatted }, color, background)) })

    open class DoneOfTotal(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(text({ "$doneFormatted/$totalFormatted" }, color, background)) })

    open class Done(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(text({ doneFormatted }, color, background)) })

    object NewLine : Text("\n")
    object Space : Text(" ")

    open class RemoveWhen(private val removeWhen: IProgressSource.() -> Boolean) :
        Text({
            var removed = false
            listOf(text {
                if (!removed && removeWhen()) {
                    removed = true
                    remove()
                }
                ""
            })
        }) {
        constructor(removeWhen: Double) : this({
            donePercent >= removeWhen
        })
    }
}

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