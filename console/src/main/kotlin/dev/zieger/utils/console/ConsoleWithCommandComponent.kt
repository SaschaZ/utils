package dev.zieger.utils.console

import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.input.KeyStroke
import kotlinx.coroutines.CoroutineScope

class ConsoleWithCommandComponent : Panel(
    LinearLayout(Direction.VERTICAL)
), FocusableConsoleComponent {

    private val consoleComponent = ConsoleComponent()
    private val commandComponent = CommandComponent { consoleComponent.outNl(it) }

    init {
        addComponent(consoleComponent)
        addComponent(commandComponent)
    }

    override var options: ConsoleOptions = ConsoleOptions()
        set(value) {
            field = value
            consoleComponent.options = options
        }
    override var scope: CoroutineScope? = null
        set(value) {
            field = value
            consoleComponent.scope = value
        }

    override var hasFocus: Boolean = false
        set(value) {
            field = value
            consoleComponent.hasFocus = value
            commandComponent.hasFocus = value
        }

    override suspend fun onKeyPressed(keyStroke: KeyStroke) {
        commandComponent.onKeyPressed(keyStroke)
        consoleComponent.onKeyPressed(keyStroke)
    }

    override fun out(str: TextString) = consoleComponent.out(str)

    override fun outNl(str: TextString) = consoleComponent.outNl(str)

    override fun textCharacter(character: Char): TextCharacterWrapper =
        TextCharacterWrapper(character)
}