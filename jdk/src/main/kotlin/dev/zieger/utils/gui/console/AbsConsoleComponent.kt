package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbstractInteractableComponent
import com.googlecode.lanterna.gui2.InteractableRenderer
import com.googlecode.lanterna.screen.Screen
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope

abstract class AbsConsoleComponent<T : AbstractInteractableComponent<T>>(
    protected val definition: ConsoleDefinition,
    protected val screen: Screen,
    protected val scope: CoroutineScope,
    private val focusable: Boolean = false
) : AbstractInteractableComponent<T>(), InteractableRenderer<T> {

    private var lastSize: TerminalSize? = null

    init {
        position = definition.position(screen.terminalSize)
        size = definition.size(screen.terminalSize)

        scope.launchEx(interval = 100.milliseconds) { checkSize() }
    }

    override fun createDefaultRenderer(): InteractableRenderer<T> = this
    override fun getPreferredSize(component: T): TerminalSize = size
    override fun getCursorLocation(component: T): TerminalPosition? = null
    override fun isFocusable(): Boolean = focusable

    private fun checkSize() {
        val prevSize = lastSize
        lastSize = screen.terminalSize?.also { newSize ->
            if (newSize != prevSize) {
                applySize(newSize)
                invalidate()
            }
        } ?: prevSize
    }

    protected open fun applySize(newSize: TerminalSize) {
        position = definition.position(newSize)
        size = definition.size(newSize)
    }
}