@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class ConsoleWindow(
    private val options: ConsoleOptions = ConsoleOptions(),
    private val scope: CoroutineScope,
    window: Window = BasicWindow()
) : Window by window, ConsoleOwnerScope {

    private val panel = Panel(AbsoluteLayout())
    private val components = LinkedList<FocusableComponent>()
    override var focusedComponent = 0
        set(value) {
            value.coerceIn(0..components.lastIndex.coerceAtLeast(0)).let {
                when {
                    it > field -> repeat(it - field) { panel.nextFocus(focusedInteractable) }
                    it < field -> repeat(field - it) { panel.previousFocus(focusedInteractable) }
                }
                components[field].focused = false
                field = it
                components[it].focused = true

                invalidate()
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

    override fun handleInput(key: KeyStroke): Boolean {
        when (key.keyType) {
            KeyType.PageUp -> focusedComponent--
            KeyType.PageDown -> focusedComponent++
            else -> scope.launch { components[focusedComponent].handleKeyStroke(key) }
        }
        return true
    }

    fun addFocusableConsoleComponent(consoleComponent: FocusableComponent) {
        consoleComponent.options = options
        consoleComponent.scope = scope
        consoleComponent.focused = focusedComponent == components.size
        components += consoleComponent
        setPositionAndSizes()
        panel.addComponent(consoleComponent)
    }

    private fun setPositionAndSizes(vertical: Boolean = false) {
        val height = textGUI.screen.terminalSize.rows / components.size
        val width = textGUI.screen.terminalSize.columns / components.size
        var top = 0
        var start = 0
        components.forEach {
            it.position = TerminalPosition(start, top)
            it.size = when (vertical) {
                true -> {
                    top += height
                    TerminalSize(textGUI.screen.terminalSize.columns, height)
                }
                false -> {
                    start += width / 2 // why?
                    TerminalSize(width, textGUI.screen.terminalSize.rows)
                }
            }
        }
    }

    override fun out(str: TextString) {
        (components[focusedComponent.coerceIn(0..components.lastIndex)] as? ConsoleScope)?.out(str)
    }

    override fun outNl(str: TextString) {
        (components[focusedComponent.coerceIn(0..components.lastIndex)] as? ConsoleScope)?.outNl(str)
    }
}