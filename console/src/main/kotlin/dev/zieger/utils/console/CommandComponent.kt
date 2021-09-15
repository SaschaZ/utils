@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import dev.zieger.utils.console.ConsoleInstances.UI_SCOPE
import dev.zieger.utils.koin.DI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.get

class CommandComponent(
    private val onNewCommand: (command: String) -> Unit
) : AbstractFocusableComponent<CommandComponent>() {

    override var di: DI? = null
        set(value) {
            field = value
            scope = value?.get(UI_SCOPE)
            options = value?.get() ?: options
        }
    override var options: ConsoleOptions = ConsoleOptions()
    private var rows: Int = 1
    private var cols: Int = 0

    override fun createDefaultRenderer(): ComponentRenderer<CommandComponent> =
        object : ComponentRenderer<CommandComponent> {

            override fun getPreferredSize(component: CommandComponent): TerminalSize =
                component.textGUI?.screen?.terminalSize?.withRows(rows) ?: TerminalSize.ONE

            override fun drawComponent(graphics: TextGUIGraphics, component: CommandComponent): Unit = graphics.run {
                var row = 0
                var col = 0
                val prefix = options.commandPrefix(TextBuilderScope(0, 0))
                val commandText =
                    prefix + +component.command * options.commandForeground / options.commandBackground
                commandText.forEach { char ->
                    if (col == 0 && row > 0) {
                        prefix.forEach { char2 ->
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
                }
                rows = row + 1
                cols = col
                cursorPosition = TerminalPosition(cols.coerceAtLeast(prefix.size), rows - 1)
                cursorPosition?.also { pos ->
                    val cursorBlinkActive =
                        !(focused && if ((System.currentTimeMillis() - lastCursorBlink.first) >= 400)
                            (!lastCursorBlink.second).also {
                                lastCursorBlink = System.currentTimeMillis() to it
                            }
                        else
                            lastCursorBlink.second)

                    setCharacter(
                        pos, TextCharacter(
                            ' ', when (cursorBlinkActive) {
                                true -> options.foreground
                                false -> options.background
                            }, when (cursorBlinkActive) {
                                true -> options.background
                                false -> options.foreground
                            }
                        )
                    )
                }
            }
        }

    private var lastCursorBlink: Pair<Long, Boolean> = System.currentTimeMillis() to false
    override var cursorPosition: TerminalPosition =
        TerminalPosition(options.commandPrefix(TextBuilderScope(0, 0)).size, 0)

    private var command: String = ""
        set(value) {
            field = value
            invalidate()
        }
    private var scope: CoroutineScope? = null
        set(value) {
            field = value
            value?.launch {
                while (isActive) {
                    invalidate()
                    delay(400)
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

