package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.LayoutData
import com.googlecode.lanterna.gui2.LayoutManager

class WeightedLinearLayout(val orientation: Orientation = Orientation.HORIZONTAL) : LayoutManager {

    companion object {

        enum class Orientation { VERTICAL, HORIZONTAL }
        data class ConsoleLayoutData(val weight: Double = 1.0) : LayoutData
    }

    private var changed: Boolean = false

    override fun getPreferredSize(components: MutableList<Component>): TerminalSize =
        components.firstOrNull()?.textGUI?.screen?.terminalSize ?: TerminalSize.ONE

    override fun doLayout(area: TerminalSize, components: MutableList<Component>) {
        val totalWeight = components.sumOf { (it.layoutData as? ConsoleLayoutData)?.weight ?: 1.0 }
        var absoluteSum = 0
        components.forEach { component ->
            val weight = (component.layoutData as? ConsoleLayoutData)?.weight ?: 1.0
            val absolute = (weight / totalWeight * when (orientation) {
                Orientation.VERTICAL -> area.rows
                Orientation.HORIZONTAL -> area.columns
            }).toInt()

            component.position = when (orientation) {
                Orientation.VERTICAL -> TerminalPosition(0, absoluteSum)
                Orientation.HORIZONTAL -> TerminalPosition(absoluteSum, 0)
            }

            component.size = when (orientation) {
                Orientation.VERTICAL -> TerminalSize(area.columns, absolute)
                Orientation.HORIZONTAL -> TerminalSize(absolute, area.rows)
            }
            absoluteSum += absolute
        }
    }

    override fun hasChanged(): Boolean = changed
}