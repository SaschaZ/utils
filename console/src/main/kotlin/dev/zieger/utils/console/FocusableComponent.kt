package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2.AbstractComponent
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.input.KeyStroke
import dev.zieger.utils.koin.DI

interface FocusableComponent : Component {

    var di: DI?
    var cursorPosition: TerminalPosition?
    var options: ConsoleOptions
    var focused: Boolean
    val focusable: Boolean
        get() = true
    var enabled: Boolean

    suspend fun handleKeyStroke(key: KeyStroke): Boolean = false
}

interface FocusableConsoleComponent : FocusableComponent, ConsoleScope

abstract class AbstractFocusableComponent<T: AbstractFocusableComponent<T>>(
    override val focusable: Boolean = true
) : AbstractComponent<T>(), FocusableComponent {

    override var options: ConsoleOptions = ConsoleOptions()
    override var cursorPosition: TerminalPosition? = null
    override var focused: Boolean = false
    override var enabled: Boolean = true
}