@file:Suppress("unused")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import dev.zieger.utils.time.duration.IDurationEx

class ProgressScope(
    source: IProgressSource,
    scope: ITextScope
) : IProgressSource by source, ITextScope by scope

sealed class ProgressEntity {

    abstract fun textWithColor(source: IProgressSource): TextWithColor

    data class Bar(
        private val size: Int = ConsoleProgressBar.DEFAULT_SIZE,
        private val foreground: IProgressColorProvider = ConsoleProgressBar.PROGRESS_COLORS,
        private val background: IProgressColorProvider = ColorGradient(),
        private val fraction: String = ConsoleProgressBar.FRACTION_2,
        private val midPart: (done: Long, max: Long) -> String? = { d, m ->
            (d / m.toDouble() * 100).let { " %4.1f%% ".format(it) }
        }
    ) : ProgressEntity() {

        private val IProgressSource.bar
            get() = ConsoleProgressBar(
                this, size = size, foreground = foreground, background = background,
                fraction = fraction, midPart = midPart
            )

        override fun textWithColor(source: IProgressSource): TextWithColor = source.bar.textWithColor
    }

    open class Text(private val text: IProgressSource.() -> TextWithColor) : ProgressEntity() {
        constructor(text: TextWithColor) : this({ text })
        constructor(text: String, color: TextColor? = null, background: TextColor? = null) :
                this(text({ text }, { color }, { background }))

        override fun textWithColor(source: IProgressSource): TextWithColor = source.text()
    }

    open class ItemsPerSecond(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ text({ unitsPerSecondFormatted }, color, background) })

    open class DoneSpeed(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ text({ doneSpeedFormatted }, color, background) })

    open class FinishedIn(maxEntities: Int = 2, color: MessageColor? = null, background: MessageColor? = null) :
        Text({ text({ finishedIn.formatDuration(maxEntities = maxEntities) }, color, background) })

    open class DoneFinishedIn(maxEntities: Int = 2, color: MessageColor? = null, background: MessageColor? = null) :
        Text({ text({ doneFinishedIn.formatDuration(maxEntities = maxEntities) }, color, background) })

    open class LastAction(
        minDuration: IDurationEx? = null,
        color: MessageColor? = null, background: MessageColor? = null
    ) : Text({
        text({
            if (minDuration?.let { it <= lastActionBefore } != false) lastActionBefore else ""
        }, color, background)
    })

    open class Remaining(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ text({ remainingFormatted }, color, background) })

    open class DoneOfTotal(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ text({ "$doneFormatted/$totalFormatted" }, color, background) })

    open class Done(color: MessageColor? = null, background: MessageColor? = null) :
        Text({ text({ doneFormatted }, color, background) })

    object NewLine : Text("\n")
    object Space : Text(" ")

    open class RemoveWhen(private val removeWhen: IProgressSource.() -> Boolean) :
        Text({
            var removed = false
            text {
                if (!removed && removeWhen()) {
                    removed = true
                    remove()
                }
                ""
            }
        }) {
        constructor(removeWhen: Double) : this({
            donePercent >= removeWhen
        })
    }
}