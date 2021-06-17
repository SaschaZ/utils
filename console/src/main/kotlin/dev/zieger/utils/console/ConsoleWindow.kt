@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package dev.zieger.utils.console

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kotlinx.coroutines.CoroutineScope
import java.util.*

class ConsoleWindow(
    private val options: ConsoleOptions = ConsoleOptions(),
    private val scope: CoroutineScope,
    window: Window = BasicWindow()
) : Window by window, ConsoleOwnerScope {

    private val panel = Panel(LinearLayout(Direction.VERTICAL))
    private val components = LinkedList<FocusableComponent>()
    override var activeComponent = 0
        set(value) {
            if (value in 0..components.lastIndex) {
                components[field].hasFocus = false
                field = value
                components[field].hasFocus = true
            }
        }

    init {
        options.run {
            position?.let { window.position = it }
            size?.let { setFixedSize(it) }
            window.decoratedSize = decoratedSize
            window.theme = SimpleTheme.makeTheme(
                false,
                foreground, background, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT, selectedForeground,
                selectedBackground, TextColor.ANSI.DEFAULT
            )
            window.setHints(hints)
        }
        component = panel
    }

    override fun textCharacter(character: Char): TextCharacterWrapper =
        TextCharacterWrapper(character, foreground = options.foreground, background = options.background)

    internal suspend fun onKeyPressed(keyStroke: KeyStroke) {
        when (keyStroke.keyType) {
            KeyType.PageUp -> activeComponent--
            KeyType.PageDown -> activeComponent++
            else -> components[activeComponent].onKeyPressed(keyStroke)
        }
    }

    fun addFocusableConsoleComponent(consoleComponent: FocusableConsoleComponent) {
        consoleComponent.options = options
        consoleComponent.scope = scope
        consoleComponent.hasFocus = activeComponent == components.size
        components += consoleComponent
        panel.addComponent(consoleComponent)
    }

    override fun out(str: TextString) {
        (components[activeComponent] as? ConsoleScope)?.out(str)
    }

    override fun outNl(str: TextString) {
        (components[activeComponent] as? ConsoleScope)?.outNl(str)
    }
}