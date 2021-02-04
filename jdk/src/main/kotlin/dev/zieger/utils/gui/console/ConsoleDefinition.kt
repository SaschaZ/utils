@file:Suppress("unused")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor.ANSI.YELLOW
import com.googlecode.lanterna.TextColor.ANSI.YELLOW_BRIGHT
import com.googlecode.lanterna.gui2.Window
import dev.zieger.utils.gui.console.Position.Absolute
import dev.zieger.utils.gui.console.Position.Relative
import kotlinx.coroutines.CoroutineScope

sealed class Position {

    abstract val value: (size: TerminalSize) -> Number

    data class Absolute(override val value: (size: TerminalSize) -> Int) : Position() {
        constructor(value: Int) : this({ value })
    }

    data class Relative(override val value: (size: TerminalSize) -> Double) : Position() {
        constructor(value: Double) : this({ value })
    }
}

val Double.rel get() = Relative(this)
val Int.abs get() = Absolute(this)

data class ConsoleDefinition(
    val topLeft: Pair<Position, Position> = 0.0.rel to 0.0.rel,
    val bottomRight: Pair<Position, Position> = 1.0.rel to 1.0.rel,
    val hasCommandInput: Boolean = true,
    val commandPrefix: TextWithColor = text(">>: ", YELLOW),
    val activeCommandPrefix: TextWithColor = text(">>: ", YELLOW_BRIGHT),
    val logMessagePrefix: TextWithColor = text(": ", YELLOW),
    private val componentFactory: (
        screen: PanelScreen,
        scope: CoroutineScope,
        window: Window,
        definition: ConsoleDefinition
    ) -> AbsConsoleComponent<*> = { scr, s, w, d -> ConsoleComponent(scr, s, w, d) }
) {
    fun createComponent(
        screen: PanelScreen,
        scope: CoroutineScope,
        window: Window
    ): List<AbsConsoleComponent<*>> = componentFactory(screen, scope, window, this).let { console ->
        if (hasCommandInput && console is ConsoleComponent)
            listOf(console, CommandComponent(this, screen, scope, console))
        else listOf(console)
    }

    fun position(size: TerminalSize): TerminalPosition =
        TerminalPosition(
            when (val position = topLeft.first) {
                is Absolute -> position.value(size)
                is Relative -> size.columns * position.value(size)
            }.toInt(),
            when (val position = topLeft.second) {
                is Absolute -> position.value(size)
                is Relative -> size.rows * position.value(size)
            }.toInt()
        )

    fun commandPosition(size: TerminalSize): TerminalPosition =
        TerminalPosition(
            when (val position = topLeft.first) {
                is Absolute -> position.value(size)
                is Relative -> size.columns * position.value(size)
            }.toInt(),
            when (val position = topLeft.second) {
                is Absolute -> position.value(size)
                is Relative -> size(size).rows + position(size).row
            }.toInt()
        )

    fun size(size: TerminalSize): TerminalSize =
        TerminalSize(
            when (val position = bottomRight.first) {
                is Absolute -> position.value(size)
                is Relative -> size.columns * position.value(size) - position(size).column
            }.toInt(),
            when (val position = bottomRight.second) {
                is Absolute -> position.value(size)
                is Relative -> size.rows * position.value(size) - position(size).row - if (hasCommandInput) 1 else 0
            }.toInt()
        )

    fun commandSize(size: TerminalSize): TerminalSize =
        TerminalSize(
            when (val position = bottomRight.first) {
                is Absolute -> position.value(size)
                is Relative -> size.columns * position.value(size) - position(size).column
            }.toInt(),
            when (val position = bottomRight.second) {
                is Absolute -> position.value(size)
                is Relative -> 1
            }.toInt()
        )
}