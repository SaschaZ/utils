@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.AbsoluteLayout
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import dev.zieger.utils.console.ConsoleInstances.SIZE_SCOPE
import dev.zieger.utils.console.ConsoleInstances.UI_SCOPE
import dev.zieger.utils.koin.DI
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.get
import java.util.*
import kotlin.math.absoluteValue

class ConsoleWindow(
    private val options: ConsoleOptions = ConsoleOptions(),
    private val di: DI,
    window: Window = BasicWindow()
) : Window by window, ConsoleOwnerScope, DI by di {

    private val panel = Panel(AbsoluteLayout())
    private val components = LinkedList<FocusableComponent>()
    override var focusedComponent = 0
        set(value) {
            (value % components.size).absoluteValue.let {
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
        get<CoroutineScope>(SIZE_SCOPE).launch {
            while (isActive) {
                if (components.isNotEmpty())
                    setPositionAndSizes()
                delay(100)
            }
        }
    }

    override fun handleInput(key: KeyStroke): Boolean {
        when (key.keyType) {
            KeyType.PageUp -> focusedComponent--
            KeyType.PageDown -> focusedComponent++
            else -> get<CoroutineScope>(UI_SCOPE).launch { components[focusedComponent].handleKeyStroke(key) }
        }
        return true
    }

    fun addFocusableConsoleComponent(consoleComponent: FocusableComponent) = get<CoroutineScope>(UI_SCOPE).launch {
        consoleComponent.di = get()
        consoleComponent.focused = focusedComponent == components.size
        components += consoleComponent
        setPositionAndSizes()
        panel.addComponent(consoleComponent)
    }.asUnit()

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

    override fun out(componentId: Int, str: TextString) {
        (components.getOrNull(componentId) as? ConsoleScope)?.out(str)
    }

    override fun outNl(componentId: Int, str: TextString) {
        (components.getOrNull(componentId) as? ConsoleScope)?.outNl(str)
    }
}