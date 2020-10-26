@file:Suppress("FunctionName")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.gui.console.LanternaConsole.Companion.lastInstance
import dev.zieger.utils.misc.format

open class ConsoleProgressBar(
    initial: Int = 0,
    private var max: Int = 100,
    private val size: Int = DEFAULT_SIZE,
    foreground: IProgressColorProvider = PROGRESS_COLORS,
    background: IProgressColorProvider = ColorGradient(0x000000),
    private val fraction: String = FRACTION_2,
    private val refresh: () -> Unit
) {

    companion object {
        const val DEFAULT_SIZE = 27

        const val FRACTION_10 = " ▏▎▍▌▋▊▉██"
        const val FRACTION_100 = " ⡀⡄⡆⡇⣇⣧⣷⣿░"
        const val FRACTION_2 = "▱▰"

        data class ProgressTestData(
            val progress: Int = 0,
            val max: Int = 100
        )

        val PROGRESS_COLORS = ColorGradient(
            0xFF0000,
            0xF40000,
            0xFF8000,
            0xFFAF00,
            0xFFF000,
            0xFFFF00,
            0xCEFF00,
            0x8EFF00,
            0x4FFF00,
            0x40FF00,
            0x00FF00
        )

        private fun buildProgress(progress: Double, size: Int, fraction: String): String {
            val progressPercent = progress.coerceIn(0.0..1.0)
            val progressStr = " %.1f%% ".format(progressPercent * 100)
            val progressDigits = (size - 7).let { it + it % 2 }
            val done = progressPercent * progressDigits
            val doneInt = done.toInt()
            val fractionIdx = ((done - doneInt) * fraction.lastIndex).toInt()
                .coerceIn(0..fraction.lastIndex)
            return (0 until progressDigits).joinToString("") { idx ->
                when {
                    idx < doneInt || progressPercent == 1.0 -> "${fraction.last()}"
                    idx == doneInt -> "${fraction[fractionIdx]}"
                    else -> "${fraction.first()}"
                }
            }.run { substring(0 until length / 2) + progressStr + substring(length / 2 until length) }
        }
    }

    private var foregroundColor = foreground(0.0)
    private var backgroundColor = background(0.0)

    var progressPercent by OnChanged(
        initial / max.toDouble(),
        map = { it.coerceIn(0.0, 1.0) },
        notifyForInitial = true
    ) {
        foregroundColor = foreground(it)
        backgroundColor = background(it)
        text = buildProgress(it, size, fraction)
        refresh()
    }

    var progress by OnChanged(initial, notifyForInitial = true) {
        progressPercent = it / max.toDouble()
    }

    var text: String = buildProgress(progressPercent, size, FRACTION_2)
        private set

    val textWithColor get() = TextWithColor({ text }, { foregroundColor }, { backgroundColor })

    fun textWithColor(block: MessageScope.(Double) -> Unit = {}): TextWithColor =
        textWithColor.copy(text = {
            block(progressPercent)
            textWithColor.text(this)
        })
}

interface IProgressColorProvider : (Double) -> TextColor {

    override fun invoke(progress: Double): TextColor = colorFor(progress)

    fun colorFor(progress: Double): TextColor
}

class ColorGradient(vararg colors: Int) :
    IProgressColorProvider {
    private val colorList = colors.toList()
    private val singleColorRange = 1.0 / (colorList.size - 1)

    private fun Double.gradientItem(from: Int, to: Int): TextColor {
        fun Int.splitColors(): Triple<Int, Int, Int> {
            val r = ((this shr 16) and 0xFF).coerceIn(0..255)
            val g = ((this shr 8) and 0xFF).coerceIn(0..255)
            val b = (this and 0xFF).coerceIn(0..255)
            return Triple(r, g, b)
        }
        val (fr, fg, fb) = from.splitColors()
        val (tr, tg, tb) = to.splitColors()

        val r = (fr + (tr - fr) * this).toInt().coerceIn(0..255)
        val g = (fg + (tg - fg) * this).toInt().coerceIn(0..255)
        val b = (fb + (tb - fb) * this).toInt().coerceIn(0..255)
        return TextColor.RGB(r, g, b)
    }

    override fun colorFor(progress: Double): TextColor {
        if (colorList.isEmpty()) return TextColor.RGB(0, 0, 0)

        val colorIndex = (progress / singleColorRange).toInt().coerceIn(0..colorList.lastIndex)
        val colorProgress = progress % singleColorRange * (colorList.size - 1)
        return colorProgress.gradientItem(
            colorList[colorIndex],
            colorList[colorList.lastIndex.coerceAtMost(colorIndex + 1)]
        )
    }

    fun reversed() = ColorGradient(*colorList.reversed().toIntArray())
}

fun PROGRESS(
    progressSource: IProgressSource,
    removeWhenComplete: Boolean = true,
    useDoneBytes: Boolean = true,
    initial: Int = 0,
    max: Int = 100,
    size: Int = ConsoleProgressBar.DEFAULT_SIZE,
    foreground: IProgressColorProvider = ConsoleProgressBar.PROGRESS_COLORS,
    background: IProgressColorProvider = ColorGradient(0x000000),
    fraction: String = ConsoleProgressBar.FRACTION_2,
    text: String? = null,
    textProvider: ((Double) -> Any)? = (text ?: progressSource.name)?.let { t -> { t } }
): Array<TextWithColor> {
    val testToUse = textProvider?.invoke(progressSource.doneBytesPercent)?.toString() ?: ""
    var speedLength = 0
    return arrayOf(
        *(textProvider?.let {
            arrayOf(TextColor.ANSI.WHITE {
                if (removeWhenComplete && progressSource.doneBytesPercent == 1.0) remove()
                testToUse
            })
        } ?: emptyArray()),
        ConsoleProgressBar(progressSource, useDoneBytes, initial, max, size, foreground, background, fraction)
            .textWithColor { if (removeWhenComplete && it == 1.0) remove() },
        TextColor.ANSI.WHITE {
            if (removeWhenComplete && progressSource.doneBytesPercent == 1.0) remove()
            " ${progressSource.mbPerSecond.format(1)?.also { speedLength = it.length + 5 }}MB/s\n" +
                    ("${(progressSource.doneBytes / 1024.0 / 1024).format(1)}MB|" +
                            "${(progressSource.totalBytes / 1024.0 / 1024).format(1)}MB - " +
                            "${progressSource.activeFor.formatDuration(maxEntities = 2)}/" +
                            progressSource.finishedIn.formatDuration(maxEntities = 2))
                        .center(size + testToUse.length + speedLength)
        }
    )
}

@Suppress("FunctionName")
fun ConsoleProgressBar(
    progressSource: IProgressSource,
    useDoneBytes: Boolean = true,
    initial: Int = 0,
    max: Int = 100,
    size: Int = ConsoleProgressBar.DEFAULT_SIZE,
    foreground: IProgressColorProvider = ConsoleProgressBar.PROGRESS_COLORS,
    background: IProgressColorProvider = ColorGradient(0x000000),
    fraction: String = ConsoleProgressBar.FRACTION_2
): ConsoleProgressBar {
    val consoleProgressBar = ConsoleProgressBar(initial, max, size, foreground, background, fraction) {
        lastInstance?.scope?.refresh()
    }
    (if (useDoneBytes) progressSource.doneBytesObservable else progressSource.doneItemsObservable)
        .observe {
            consoleProgressBar.progressPercent =
                if (useDoneBytes) progressSource.doneBytesPercent else progressSource.doneItemsPercent
        }
    return consoleProgressBar
}

private fun String.center(size: Int): String {
    return when {
        length < size -> ((size - length) / 2).let { diff -> diff.spaces + this }
        else -> this
    }
}

private val Int.spaces get() = (0 until this).joinToString("") { " " }