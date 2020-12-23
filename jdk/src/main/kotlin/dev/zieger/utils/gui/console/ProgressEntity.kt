package dev.zieger.utils.gui.console

import dev.zieger.utils.gui.console.ProgressEntity.Text
import dev.zieger.utils.time.duration.IDurationEx

class SingleTextScope(
    source: IProgressSource,
    scope: MessageScope
) : IProgressSource by source, MessageScope by scope

@Suppress("FunctionName")
fun SingleText(text: SingleTextScope.() -> Any, color: MessageColor? = null, background: MessageColor? = null) =
    Text { listOf(text({ SingleTextScope(this@Text, this).text() }, color, background)) }

sealed class ProgressEntity {

    abstract fun textWithColor(source: IProgressSource): List<TextWithColor>

    data class Bar(
        private val size: Int = ConsoleProgressBar.DEFAULT_SIZE,
        private val foreground: IProgressColorProvider = ConsoleProgressBar.PROGRESS_COLORS,
        private val background: IProgressColorProvider = ColorGradient(),
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