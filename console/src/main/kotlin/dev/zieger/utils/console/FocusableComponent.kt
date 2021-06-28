package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2.AbstractComponent
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.InputFilter
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.input.KeyStroke
import kotlinx.coroutines.CoroutineScope

interface OptionsAndScopeHolder {
    var options: ConsoleOptions
    var scope: CoroutineScope?
}

interface FocusableComponent : Component, OptionsAndScopeHolder {
    var cursorPosition: TerminalPosition?
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

    override lateinit var options: ConsoleOptions
    override var scope: CoroutineScope? = null
    override var cursorPosition: TerminalPosition? = null
    override var focused: Boolean = false
    override var enabled: Boolean = true
}