@file:Suppress("unused")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.WHITE

sealed class ProgressEntity {

    abstract fun textWithColor(source: IProgressSource): List<TextWithColor>

    data class Bar(
        private val size: Int = ConsoleProgressBar.DEFAULT_SIZE,
        private val foreground: IProgressColorProvider = ConsoleProgressBar.PROGRESS_COLORS,
        private val background: IProgressColorProvider = ColorGradient(0x000000),
        private val fraction: String = ConsoleProgressBar.FRACTION_2,
        private val midPart: (done: Int, max: Int) -> String? = { d, m ->
            (d / m.toDouble() * 100).let { " %.1f%% ".format(it) }
        }
    ) : ProgressEntity() {

        private val IProgressSource.bar
            get() = ConsoleProgressBar(
                this, done.toInt(), total.toInt(), size = size,
                foreground = foreground, background = background, fraction = fraction, midPart = midPart
            )

        override fun textWithColor(source: IProgressSource): List<TextWithColor> = listOf(source.bar.textWithColor)
    }

    open class Text(private val text: IProgressSource.() -> List<TextWithColor>) : ProgressEntity() {
        constructor(text: String, color: MessageColor? = null, background: MessageColor? = null) :
                this({ listOf(TWC({ text }, color, background)) })

        override fun textWithColor(source: IProgressSource): List<TextWithColor> = source.text()
    }

    class ItemsPerSecond(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(TWC({ unitsPerSecondFormatted }, color, background)) }) {
        constructor(color: TextColor? = null, background: TextColor? = null) : this({ color }, { background })
    }

    class Remaining(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(TWC({ remainingFormatted }, color, background)) }) {
        constructor(color: TextColor? = null, background: TextColor? = null) : this({ color }, { background })
    }

    class DoneOfTotal(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ listOf(TWC({ "$doneFormatted/$totalFormatted" }, color, background)) }) {
        constructor(color: TextColor? = null, background: TextColor? = null) : this({ color }, { background })
    }

    object NewLine : Text("\n")
    object Space : Text(" ")

    class RemoveWhen(private val removeWhen: IProgressSource.() -> Boolean) :
        Text({ listOf(TWC({ if (removeWhen()) remove(); "" })) }) {
        constructor(removeWhen: Double) : this({ donePercent == removeWhen })
    }
}

@Suppress("FunctionName")
fun PROGRESS(
    progressSource: IProgressSource,
    vararg entities: ProgressEntity
): List<TextWithColor> = entities.flatMap { it.textWithColor(progressSource) }

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
    postTextProvider: ((progress: Double) -> Any?)? = { postText },
): Array<TextWithColor> {
    fun MessageScope.removeWhenComplete() {
        if (removeWhenComplete && progressSource.donePercent >= 0.999) remove()
    }

    fun preText(): TextWithColor = TWC({
        removeWhenComplete()
        preTextProvider?.invoke(progressSource.donePercent)?.toString() ?: ""
    })

    val hideBar = max == -1
    fun bar(): TextWithColor? =
        if (hideBar) null else ConsoleProgressBar(progressSource, initial, max, size, foreground, background, fraction)
            .textWithColor { removeWhenComplete() }

    fun afterBarText(): TextWithColor = TWC({
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
    })

    fun postText(): TextWithColor = TWC({
        removeWhenComplete()
        postTextProvider?.invoke(progressSource.donePercent)?.toString() ?: ""
    })

    return listOfNotNull(preText(), bar(), afterBarText(), postText()).toTypedArray()
}