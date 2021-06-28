@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun CommandComponent(
    options: ConsoleOptions,
    onNewCommand: (command: String) -> Unit
): CommandComponent =
    CommandComponent(onNewCommand).also { it.options = options }

class CommandComponent(
    private val onNewCommand: (command: String) -> Unit
) : AbstractFocusableComponent<CommandComponent>() {

    private var rows: Int = 1
    private var cols: Int = 0

    override fun createDefaultRenderer(): ComponentRenderer<CommandComponent> =
        object : ComponentRenderer<CommandComponent> {

            override fun getPreferredSize(component: CommandComponent): TerminalSize =
                component.textGUI?.screen?.terminalSize?.withRows(rows) ?: TerminalSize.ONE

            override fun drawComponent(graphics: TextGUIGraphics, component: CommandComponent) = graphics.run {
                var row = 0
                var col = 0
                val commandText =
                    options.commandPrefix + +component.command * options.commandForeground / options.commandBackground
                commandText().map { it() }.forEach { char ->
                    if (col == 0 && row > 0) {
                        options.commandNewLinePrefix().map { it() }.forEach { char2 ->
                            setCharacter(col, row, char2.textCharacter)
                            col++
                        }
                    }
                    when (char.character) {
                        '\n' -> {
                            col = 0
                            row++
                        }
                        else -> {
                            setCharacter(col, row, char.textCharacter)
                            col++
                            if (col == component.size.columns) {
                                col = 0
                                row++
                            }
                        }
                    }
                    cursorPosition?.also { pos ->
                        setCharacter(
                            pos, TextCharacter(
                                ' ', when (cursorBlink) {
                                    true -> options.foreground
                                    false -> options.background
                                }, when (cursorBlink) {
                                    true -> options.background
                                    false -> options.foreground
                                }
                            )
                        )
                    }
                }
                rows = row + 1
                cols = if (command.isEmpty()) col else col + 1
            }
        }

    override var cursorPosition: TerminalPosition? = TerminalPosition(2, 0)
    private var cursorBlink = false
        set(value) {
            field = value
            invalidate()
        }

    private var command: String = ""
        set(value) {
            field = value
            cursorPosition = TerminalPosition(cols.coerceAtLeast(options.commandPrefix().size), rows - 1)
            invalidate()
        }
    override var scope: CoroutineScope? = null
        set(value) {
            field = value
            value?.launch {
                while (true) {
                    cursorBlink = if (focused) !cursorBlink else true
                    delay(450)
                }
            }
        }

    override suspend fun handleKeyStroke(key: KeyStroke): Boolean {
        when (key.keyType) {
            KeyType.Character -> command += key.character
            KeyType.Backspace -> command = command.ifEmpty { null }?.take(command.length - 1) ?: command
            KeyType.Tab -> command += (0..(command.length % 4).let { if (it == 0) 3 else it }).joinToString("") { " " }
            KeyType.Enter -> {
                if (key.isCtrlDown) {
                    command += '\n'
                } else {
                    onNewCommand(command)
                    command = ""
                }
            }
            else -> Unit
        }
        return true
    }
}

