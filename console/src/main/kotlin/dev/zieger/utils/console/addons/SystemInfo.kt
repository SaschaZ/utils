package dev.zieger.utils.console.addons

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import dev.zieger.utils.console.components.AbstractFocusableComponent
import dev.zieger.utils.misc.nullWhen
import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import oshi.hardware.HardwareAbstractionLayer

class SystemInfoComponent(
    private val foreground: (Float) -> TextColor = { TextColor.ANSI.WHITE },
    private val background: (Float) -> TextColor = { TextColor.ANSI.BLUE }
) : AbstractFocusableComponent<SystemInfoComponent>() {

    override val focusable: Boolean = false

    private val si = SystemInfo()
    private val hal: HardwareAbstractionLayer = si.hardware
    private val cpu: CentralProcessor = hal.processor

    private var prevTicks: LongArray = LongArray(8)
    private var prevLoad: Float = 0f

    private fun cpuPercent(): Float {
        val cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks)
        prevTicks = cpu.systemCpuLoadTicks
        prevLoad = cpuLoad.toFloat().nullWhen { it <= 0f } ?: prevLoad
        return prevLoad
    }

    private fun memPercent(): Float = 1f - hal.memory.run { available / total }.toFloat()

    override fun createDefaultRenderer(): ComponentRenderer<SystemInfoComponent> =
        object : ComponentRenderer<SystemInfoComponent> {
            override fun getPreferredSize(component: SystemInfoComponent?): TerminalSize = TerminalSize(14, 2)

            override fun drawComponent(graphics: TextGUIGraphics, component: SystemInfoComponent) {
                val cpuPercent = component.cpuPercent()
                graphics.progressBar(
                    TOP_LEFT_CORNER,
                    "CPU ".map { TextCharacter(it) },
                    10,
                    cpuPercent,
                    component.foreground(cpuPercent),
                    component.background(cpuPercent)
                )
                val memPercent = component.memPercent()
                graphics.progressBar(
                    TOP_LEFT_CORNER.withRelative(0, 1),
                    "MEM ".map { TextCharacter(it) },
                    10,
                    memPercent,
                    component.foreground(memPercent),
                    component.background(memPercent)
                )
            }
        }
}

fun TextGUIGraphics.progressBar(
    position: TerminalPosition, prefix: List<TextCharacter>, width: Int, percent: Float,
    foreground: TextColor = TextColor.ANSI.WHITE,
    background: TextColor = TextColor.ANSI.BLACK
) {
    prefix.forEachIndexed { idx, character ->
        setCharacter(position.withRelative(idx, 0), character)
    }
    (0..width).forEach { idx ->
        val rel = idx / width.toFloat()
        val percent1 = width / 100f
        val char = when {
            idx / percent1 < percent -> LINEAR_PROGRESS_BAR.last()
            (idx - 1) / percent1 < percent ->
                LINEAR_PROGRESS_BAR[(percent % percent1 / percent1 * LINEAR_PROGRESS_BAR.length).toInt()]
            else -> ' '
        }
        setCharacter(position.withRelative(prefix.size + idx, 0), TextCharacter(char, foreground, background))
    }
}

private const val LINEAR_PROGRESS_BAR = " ▏▎▍▌▋▊▉█"