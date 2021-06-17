package dev.zieger.utils.console

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import java.util.*

class ConsoleRenderer : ComponentRenderer<ConsoleComponent> {

    override fun getPreferredSize(component: ConsoleComponent): TerminalSize =
        component.textGUI.screen.terminalSize

    override fun drawComponent(graphics: TextGUIGraphics, component: ConsoleComponent) {
        var bufferLine = 0
        graphics.apply {
            LinkedList(component.buffer).forEachIndexed { _, charBlock ->
                charBlock.forEachIndexed { _, chars ->
                    chars().forEachIndexed { col, char ->
                        setCharacter(
                            component.position.column + col,
                            component.position.row + bufferLine + component.scrollIdx,
                            char().textCharacter.run {
                                if (!component.hasFocus) withModifier(SGR.ITALIC) else this
                            }
                        )
                    }
                    bufferLine++
                }
            }
        }
    }
}