package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.gui2.AbstractComponent
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

class CommandComponent(
    private val onNewCommand: suspend (command: String) -> Unit
) : AbstractComponent<CommandComponent>(), FocusableComponent {

    override fun createDefaultRenderer(): ComponentRenderer<CommandComponent> =
        object : ComponentRenderer<CommandComponent> {
            override fun getPreferredSize(component: CommandComponent): TerminalSize = TerminalSize(
                component.command.length % component.textGUI.screen.terminalSize.columns,
                component.command.length / component.textGUI.screen.terminalSize.columns + 1 + component.command.count { it == '\n' }
            )

            override fun drawComponent(graphics: TextGUIGraphics, component: CommandComponent) = graphics.run {
                var newLine = 0
                component.command.split('\n').forEach {
                    it.forEachIndexed { idx, c ->
                        setCharacter(
                            idx % component.size.columns,
                            idx / component.size.columns.let { col -> if (col == 0) idx else col } + newLine,
                            TextCharacter(c)
                        )
                    }
                    if (it.isNotEmpty())
                        newLine += it.length / component.size.columns.let { c -> if (c == 0) it.length else c } + 1
                }
            }
        }

    private var command: String = ""
        set(value) {
            field = value
            textGUI.screen.cursorPosition = position
                .withRelativeColumn(command.length % size.columns.let { if (it == 0) 1 else it })
                .withRelativeRow(command.length / size.columns.let { if (it == 0) 1 else it })
            invalidate()
        }

    override var hasFocus: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    override suspend fun onKeyPressed(keyStroke: KeyStroke) {
        when (keyStroke.keyType) {
            KeyType.Character -> command += keyStroke.character
            KeyType.Backspace -> command = command.ifEmpty { null }?.take(command.length - 1) ?: command
            KeyType.Tab -> command += (0..(command.length % 4).let { if (it == 0) 3 else it }).map { ' ' }
            KeyType.Enter -> {
                if (keyStroke.isCtrlDown) {
//                    position = position.withRelativeRow(-1)
//                    size = size.withRelativeRows(1)
                    command += '\n'
                } else {
                    onNewCommand(command)
                    command = ""
                }
            }
            else -> Unit
        }
    }
}