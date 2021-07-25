package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import dev.zieger.utils.koin.DI
import org.koin.core.component.get

class ConsoleWithCommandComponent : Panel(LinearLayout(Direction.VERTICAL)), ConsoleScope, FocusableComponent {

    override var di: DI? = null
        set(value) {
            field = value
            consoleComponent.di = value
            commandComponent.di = value
            options = value?.get() ?: options
        }
    override var options: ConsoleOptions = ConsoleOptions()
    private val consoleComponent = ConsoleComponent()
    private val commandComponent = CommandComponent { consoleComponent.outNl(it) }
    override var cursorPosition: TerminalPosition? = null
    override var focused: Boolean = false
        set(value) {
            field = value
            commandComponent.focused = value
        }
    override var enabled: Boolean = true

    override fun setPosition(position: TerminalPosition): Panel {
        consoleComponent.position = position
        commandComponent.position = position.withRelativeRow(size.rows - 1)
        return super.setPosition(position)
    }

    override fun setSize(size: TerminalSize): Panel {
        consoleComponent.size = size.withRelativeRows(-1)
        commandComponent.size = size.withRows(1)
        return super.setSize(size)
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
    override fun release() = consoleComponent.release()
}