package dev.zieger.utils.console.components

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbsoluteLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.input.KeyStroke
import dev.zieger.utils.console.dto.ConsoleOptions
import dev.zieger.utils.koin.DI
import dev.zieger.utils.misc.nullWhen
import kotlin.properties.Delegates

open class GroupComponent(private val children: List<FocusableComponent>) :
    Panel(AbsoluteLayout()), FocusableComponent {

    override var di: DI? by Delegates.observable(null) { _, _, di ->
        children.forEach { it.di = di }
    }

    override var cursorPosition: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER
    override var options: ConsoleOptions = ConsoleOptions()

    override var focused: Boolean
        get() = children.any { it.focused }
        set(value) {
            children.firstOrNull { it.focusable }?.focused = value
        }
    override var enabled: Boolean
        get() = children.any { it.enabled }
        set(value) = children.forEach { it.enabled = value }

    init {
        children.forEach { addComponent(it) }
    }

    override fun setPosition(position: TerminalPosition): Panel {
        var height = 0
        children.forEach {
            it.position = position.withRelative(0, height)
            height += it.preferredSize.rows
        }
        return super.setPosition(position)
    }

    override fun setSize(size: TerminalSize): Panel {
        val relSize = size.columns.toFloat() / children.sumOf { it.preferredSize.rows }
        children.forEach {
            it.size = it.preferredSize.withRows((it.preferredSize.rows * relSize).toInt())
        }
        return super.setSize(size)
    }

    override suspend fun handleKeyStroke(key: KeyStroke): Boolean =
        children.filter { it.focused }.any { it.handleKeyStroke(key) }.nullWhen { !it }
            ?: children.filter { !it.focused }.any { it.handleKeyStroke(key) }
}