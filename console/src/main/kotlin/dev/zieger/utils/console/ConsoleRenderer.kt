package dev.zieger.utils.console

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import dev.zieger.utils.console.components.ConsoleComponent
import dev.zieger.utils.misc.nullWhen

class ConsoleRenderer : ComponentRenderer<ConsoleComponent> {

    override fun getPreferredSize(component: ConsoleComponent?): TerminalSize =
        component?.textGUI?.screen?.terminalSize ?: TerminalSize.ONE

    override fun drawComponent(graphics: TextGUIGraphics, component: ConsoleComponent) {
        graphics.apply {
            component.renderBuffer.toScreenBuffer(size.columns, component).run {
                component.renderedLines = size + 1
                forEach { (line, str) ->
                    str.forEach { (col, char) ->
                        setCharacter(
                            component.position.column + col,
                            component.position.row + line + component.scrollIdx,
                            char.textCharacter.run {
                                if (!component.focused) withModifier(SGR.ITALIC) else this
                            })
                    }
                }
            }
        }
    }

    private fun List<TextBuilder>.toScreenBuffer(
        width: Int,
        component: ConsoleComponent
    ): Map<Int, Map<Int, TextCharacterWrapper>> {
        val options = component.options
        val buffer = HashMap<Int, MutableMap<Int, TextCharacterWrapper>>()
        var nlBecauseOfCommand = true
        var line = 0
        var col = 0
        var lastPrefixLength = 0

        forEach { str ->
            val scope = TextBuilderScope(line, col)
            str(scope).forEach { c ->
                if (col == 0) {
                    val prefix =
                        if (nlBecauseOfCommand) options.outputPrefix(scope) else options.outputNewLinePrefix(scope)
                    prefix.forEach {
                        buffer.getOrPut(line) { HashMap() }[col++] = it
                    }
                    lastPrefixLength = prefix.size
                }

                c.nullWhen {
                    (it.character == '\b').also { backSpace ->
                        if (backSpace) {
                            col -= 2
                            if (col < 0) {
                                line--
                                col = (buffer[line]?.size ?: 1) - 1
                            }
                            buffer[line]?.remove(col)
                        }
                    }
                }?.nullWhen {
                    (it.character == '\n').also { newLine ->
                        if (newLine && col > lastPrefixLength) {
                            nlBecauseOfCommand = false
                            col = 0
                            line++
                        } else if (newLine) {
                            nlBecauseOfCommand = false
                        }
                    }
                }?.nullWhen {
                    (it.character == '\r').also { newLine ->
                        if (newLine && col >= lastPrefixLength) {
                            nlBecauseOfCommand = true
                            col = 0
                            line++
                        } else if (newLine) {
                            nlBecauseOfCommand = true
                        }
                    }
                }?.let {
                    when (it.character) {
                        '\t' -> {
                            col = (col + 4 - lastPrefixLength).let { newCol -> newCol - newCol % 4 } + lastPrefixLength
                            if (col >= width) {
                                col = 0
                                line++
                            }
                            null
                        }
                        else -> it
                    }
                }?.also {
                    buffer.getOrPut(line) { HashMap() }[col++] = it
                    if (col == width) {
                        nlBecauseOfCommand = false
                        col = 0
                        line++
                    }
                }.also {
                    component.cursorPosition = TerminalPosition(col, line)
                }
            }
        }
        return buffer
    }
}