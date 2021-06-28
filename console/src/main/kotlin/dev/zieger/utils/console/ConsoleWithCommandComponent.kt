package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kotlinx.coroutines.CoroutineScope

class ConsoleWithCommandComponent : Panel(LinearLayout(Direction.VERTICAL)), ConsoleScope, FocusableComponent {

    private val consoleComponent = ConsoleComponent()
    private val commandComponent = CommandComponent { consoleComponent.outNl(it) }
    override var cursorPosition: TerminalPosition? = null
    override var focused: Boolean = false
        set(value) {
            field = value
            commandComponent.focused = value
        }
    override var enabled: Boolean = true

    override var options: ConsoleOptions = ConsoleOptions()
        set(value) {
            field = value
            commandComponent.options = value
            consoleComponent.options = value
        }

    override var scope: CoroutineScope? = null
        set(value) {
            field = value
            commandComponent.scope = value
            consoleComponent.scope = value
        }

    override suspend fun handleKeyStroke(key: KeyStroke): Boolean {
        when (key.keyType) {
            KeyType.ArrowUp,
            KeyType.ArrowDown -> consoleComponent.handleKeyStroke(key)
            else -> commandComponent.handleKeyStroke(key)
        }
        return true
    }

    init {
        addComponent(consoleComponent)
        addComponent(commandComponent)
    }

    override fun out(str: TextString) = consoleComponent.out(str)
    override fun outNl(str: TextString) = consoleComponent.outNl(str)
}