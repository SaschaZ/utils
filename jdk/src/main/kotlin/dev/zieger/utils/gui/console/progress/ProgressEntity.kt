@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package dev.zieger.utils.gui.console.progress

import com.googlecode.lanterna.TextColor
import dev.zieger.utils.gui.console.*
import dev.zieger.utils.time.duration.IDurationEx

interface IProgressTextScope : IProgressSource, ITextScope

class ProgressTextScope(
    source: IProgressSource,
    scope: ITextScope
) : IProgressTextScope, IProgressSource by source, ITextScope by scope

interface IProgressColorScope : IProgressSource, IColorScope

class ProgressColorScope(
    source: IProgressSource,
    scope: IColorScope
) : IProgressColorScope, IProgressSource by source, IColorScope by scope

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

    open class Text(
        private val text: IProgressTextScope.() -> Any,
        private val foreground: (IProgressColorScope.() -> TextColor?)? = null,
        private val background: (IProgressColorScope.() -> TextColor?)? = null
    ) : ProgressEntity() {
        constructor(text: IProgressTextScope.() -> Any) : this(text, null, null)

        constructor(text: TextWithColor) :
                this({ text.text(this) },
                    { text.color?.invoke(this) },
                    { text.background?.invoke(this) })

        constructor(text: String, color: TextColor? = null, background: TextColor? = null) :
                this(text({ text }, { color }, { background }))

        override fun textWithColor(source: IProgressSource): TextWithColor = source.run {
            TextWithColor({ ProgressTextScope(this@run, this).text() },
                { foreground?.invoke(ProgressColorScope(this@run, this)) },
                { background?.invoke(ProgressColorScope(this@run, this)) })
        }
    }

    open class ItemsPerSecond(
        color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({ unitsPerSecondFormatted }, color, background)

    open class DoneSpeed(
        color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({ doneSpeedFormatted }, color, background)

    open class FinishedIn(
        maxEntities: Int = 2, color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({ finishedIn.formatDuration(maxEntities = maxEntities) }, color, background)

    open class DoneFinishedIn(
        maxEntities: Int = 2, color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({ doneFinishedIn.formatDuration(maxEntities = maxEntities) }, color, background)

    open class LastAction(
        minDuration: IDurationEx? = null,
        color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({
        if (minDuration?.let { it <= lastActionBefore } != false) lastActionBefore else ""
    }, color, background)

    open class Remaining(
        color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({ remainingFormatted }, color, background)

    open class DoneOfTotal(
        color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({ "$doneFormatted/$totalFormatted" }, color, background)

    open class Done(
        color: (IProgressColorScope.() -> TextColor?)? = null,
        background: (IProgressColorScope.() -> TextColor?)? = null
    ) : Text({ doneFormatted }, color, background)

    object NewLine : Text("\n")
    object Space : Text(" ")
}

fun RemoveWhen(removeWhen: IProgressSource.() -> Boolean): ProgressEntity.Text {
    var removed = false
    return ProgressEntity.Text {
        if (!removed && removeWhen()) {
            removed = true
            remove()
        }
        ""
    }
}

fun RemoveWhen(remove: Double): ProgressEntity.Text = RemoveWhen { donePercent >= remove }