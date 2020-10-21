package dev.zieger.utils.gui

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.gui.ProgressBar.Companion.DEFAULT_SIZE
import dev.zieger.utils.gui.ProgressBar.Companion.PROGRESS_COLORS
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking

@Suppress("FunctionName")
fun <M> IModelHolder<M>.ProgressBar(
    initial: Int = 0, max: Int = 100, size: Int = DEFAULT_SIZE,
    foreground: IProgressColorProvider = PROGRESS_COLORS,
    background: IProgressColorProvider = ColorGradient(0x000000),
    fraction: String = ProgressBar.FRACTION_2, update: (suspend M.() -> Double)? = null
) = ProgressBar(initial, max, size, foreground, background, fraction, this, update)

open class ProgressBar<M>(
    initial: Int = 0,
    private var max: Int = 100,
    private val size: Int = DEFAULT_SIZE,
    foreground: IProgressColorProvider = PROGRESS_COLORS,
    background: IProgressColorProvider = ColorGradient(0x000000),
    private val fraction: String = FRACTION_2,
    modelHolder: IModelHolder<M>,
    private val update: (suspend M.() -> Double)? = null
) : LabelEx<M>(
    modelHolder = modelHolder,
    textProvider = { update?.let { modelHolder.model.it() } ?: modelHolder.model }) {

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

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            val terminal = DefaultTerminalFactory().createTerminal()
            val screen: Screen = TerminalScreen(terminal)
            val gui: MultiWindowTextGUI
            val window = BasicWindow("fooboo").apply {
                setHints(hints.toMutableList().apply { addAll(listOf(Window.Hint.CENTERED, Window.Hint.EXPANDED)) })
                component = PanelEx(
                    ProgressTestData(), layoutManager = GridLayout(2)
                ).apply {
                    addComponent(
                        ProgressBar(
                            modelHolder = this,
                            update = { progress / max.toDouble() })
                            .apply {
                                var cnt = 0
                                var reverse = false
                                DefaultCoroutineScope().launchEx(interval = 5.seconds) {
                                    updateModel {
                                        ProgressTestData(
                                            (if (reverse) --cnt else ++cnt).coerceIn(0..100), 100
                                        )
                                    }
                                    reverse = if (cnt == 105 || cnt == -5) !reverse else reverse
                                }
                            })
                }.withBorder(Borders.doubleLineBevel())
            }

            screen.startScreen()
            gui = MultiWindowTextGUI(
                screen, DefaultWindowManager(), EmptySpace(
                    TextColor.ANSI.BLACK,
                    TerminalSize.ONE
                )
            )
            gui.theme = LanternaThemes.getRegisteredTheme("businessmachine")
            gui.addWindowAndWait(window)
        }.asUnit()

        private fun buildProgress(progress: Double, size: Int, fraction: String): String {
            val progressStr = " %.1f%% ".format(progress * 100)
            val progressDigits = (size - 7).let { it + it % 2 }
            val done = progress * progressDigits
            val doneInt = done.toInt()
            val fractionIdx = ((done - doneInt) * fraction.lastIndex).toInt().coerceIn(0..fraction.lastIndex)
            return (0 until progressDigits).joinToString("") { idx ->
                when {
                    idx < doneInt || progress == 1.0 -> "${fraction.last()}"
                    idx == doneInt -> "${fraction[fractionIdx]}"
                    else -> "${fraction.first()}"
                }
            }.run { substring(0 until length / 2) + progressStr + substring(length / 2 until length) }
        }
    }

    init {
        foregroundColor = foreground(0.0)
        backgroundColor = background(0.0)
    }

    var progress by OnChanged(initial) {
        progressPercent = it / max.toDouble()
    }

    var progressPercent by OnChanged(initial / max.toDouble()) {
        foregroundColor = foreground(it)
        backgroundColor = background(it)
        text = buildProgress(it, size, fraction)
    }

    override suspend fun updateModel(block: M.() -> M) {
        progressPercent = update?.invoke(model.block()) ?: progressPercent
    }
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