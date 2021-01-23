package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import dev.zieger.utils.gui.console.Position.Absolute
import dev.zieger.utils.gui.console.Position.Relative
import dev.zieger.utils.log2.calls.logV

sealed class Position {

    abstract val value: Number

    data class Absolute(override val value: Int) : Position()
    data class Relative(override val value: Double) : Position()
}

val Double.rel get() = Relative(this)
val Int.abs get() = Absolute(this)

data class ConsoleDefinition(
    val topLeft: Pair<Position, Position> = 0.0.rel to 0.0.rel,
    val bottomRight: Pair<Position, Position> = 1.0.rel to 1.0.rel,
    val hasCommandInput: Boolean = true,
    val commandPrefix: TextWithColor = text(">>: ", TextColor.ANSI.YELLOW),
    val logMessagePrefix: TextWithColor = commandPrefix
) {
    fun position(size: TerminalSize): TerminalPosition =
        TerminalPosition(
            when (val position = topLeft.first) {
                is Absolute -> position.value
                is Relative -> size.columns * position.value
            }.toInt(),
            when (val position = topLeft.second) {
                is Absolute -> position.value
                is Relative -> size.rows * position.value
            }.toInt()
        ) logV { "position $topLeft -> $it with $size" }

    fun commandPosition(size: TerminalSize): TerminalPosition =
        TerminalPosition(
            when (val position = topLeft.first) {
                is Absolute -> position.value
                is Relative -> size.columns * position.value
            }.toInt(),
            when (val position = topLeft.second) {
                is Absolute -> position.value
                is Relative -> size(size).rows + position(size).row
            }.toInt()
        ) logV { "commandPosition $topLeft -> $it with $size" }

    fun size(size: TerminalSize): TerminalSize =
        TerminalSize(
            when (val position = bottomRight.first) {
                is Absolute -> position.value
                is Relative -> size.columns * position.value - position(size).column
            }.toInt(),
            when (val position = bottomRight.second) {
                is Absolute -> position.value
                is Relative -> size.rows * position.value - position(size).row - if (hasCommandInput) 1 else 0
            }.toInt()
        ) logV { "size $bottomRight -> $it with $size" }

    fun commandSize(size: TerminalSize): TerminalSize =
        TerminalSize(
            when (val position = bottomRight.first) {
                is Absolute -> position.value
                is Relative -> size.columns * position.value - position(size).column
            }.toInt(),
            when (val position = bottomRight.second) {
                is Absolute -> position.value
                is Relative -> 1
            }.toInt()
        ) logV { "commandSize $bottomRight -> $it with $size" }
}