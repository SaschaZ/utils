@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.log.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.minutes
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.*
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*

typealias ScreenBuffer = List<List<List<Triple<Char, TextColor, TextColor>>>>

class LanternaConsole(
    val position: TerminalPosition = TOP_LEFT_CORNER,
    private val preferredSize: TerminalSize? = null,
    bufferSize: Int = BUFFER_SIZE,
    private val cs: CoroutineScope = DefaultCoroutineScope()
) {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            LanternaConsole(TerminalPosition(10, 10), TerminalSize(20, 20)).scope {
                outNl("FooBooFo°\b\n°\boBooFooBooFoFoFooBFooFFoFooBoooBooFooBooooBooBoooFFooBooooBooFooBoooFoFooFooBooBooFFooBooooBoooBoooBoooBooFooBooFooBoo")
                outNl("mooo\t\tbooo")
                outNl(*arrayOf(WHITE("määäää")) + GREEN("foo", YELLOW), RED(WHITE) { "boo" }, BLUE("blub"))

                var startIdx = 0
                var printed = 0
                var job: Job? = null
                outNl(WHITE {
                    startIdx++
                    if (printed == 0) {
                        job = job ?: launchEx(interval = 2.seconds, delayed = 2.seconds) { printed++; refresh() }
                        " boofoo$printed "
                    } else "  DU HURENSOHN$printed  "
                })
                repeat(500) { out(WHITE { "${it + startIdx}|" }, autoRefresh = false) }
                outNl()
                refresh()

                outNl("hey")
                outNl("du")
                outNl("krasser")
                outNl("typ :-*")
                delay(1.minutes)
            }
        }.asUnit()

        private const val BUFFER_SIZE = 4098
        private const val SCROLL_DELTA = 5
        private const val SCROLL_MAX = 1024
        private const val COMMAND_PREFIX = "\$: "
    }

    private val screen: Screen = DefaultTerminalFactory().createScreen()
    private val size: TerminalSize
        get() = preferredSize ?: screen.terminalSize

    private val buffer = FiFo<List<TextWithColor>>(bufferSize)
    private var bufferLines = 0
    private var scrollIdx = 0
    private var commandInput = ""
    private var lastSize: TerminalSize = screen.terminalSize

    init {
        cs.launchEx {
            while (isActive) onKeyPressed(executeNativeBlocking { screen.readInput() })
        }
        cs.launchEx(interval = 100.milliseconds) {
            checkSize()
        }
    }

    private fun checkSize() {
        val prevSize = lastSize
        lastSize = screen.doResizeIfNecessary() ?: lastSize
        if (lastSize != prevSize) onBufferChanged()
    }

    private fun scroll(delta: Int) {
        val newDelta = when {
            delta >= 0 -> min(delta, max(0, bufferLines - size.rows - scrollIdx + 2))
            else -> max(-scrollIdx, delta)
        }
        screen.scrollLines(0, SCROLL_MAX, newDelta)
        scrollIdx += newDelta
        onBufferChanged()
    }

    private suspend fun onKeyPressed(keyStroke: KeyStroke) = when (keyStroke.keyType) {
        ArrowUp -> scroll(SCROLL_DELTA)
        ArrowDown -> scroll(-SCROLL_DELTA)
        PageUp -> scroll(5 * SCROLL_DELTA)
        PageDown -> scroll(5 * -SCROLL_DELTA)
        End -> if (keyStroke.isCtrlDown) scroll(-SCROLL_MAX) else Unit
        Home -> if (keyStroke.isCtrlDown) scroll(SCROLL_MAX) else Unit
        Character -> {
            commandInput += keyStroke.character
            onBufferChanged()
        }
        Backspace -> if (commandInput.isNotEmpty()) {
            commandInput = commandInput.take(commandInput.length - 1)
            onBufferChanged()
        } else Unit
        Enter -> {
            val newCommand = commandInput.removePrefix(COMMAND_PREFIX)
            Scope().outNl(newCommand)
            commandInput = ""
            onBufferChanged()
            launchEx { onNewCommand(newCommand) }.asUnit()
        }
        else -> Unit
    }

    private fun onBufferChanged() {
        screen.clear()
        var nlIdx = 0
        val stringArray = buffer.buildColorStringArrays()
        val rows = size.rows
        stringArray.reversed().forEach { messages ->
            messages.reversed().forEach { m ->
                var columnIdx = 0
                val lineIdx = rows - nlIdx++ + scrollIdx - 3
                if (lineIdx in 0..rows - 3) m.forEach { (c, col, back) ->
                    screen.setCharacter(
                        position.column + columnIdx++,
                        position.row + lineIdx,
                        TextCharacter(c, col, back)
                    )
                }
            }
        }
        bufferLines = nlIdx

        val command = commandInput.takeLast(size.columns - COMMAND_PREFIX.length - 2)
        COMMAND_PREFIX.forEachIndexed { i, c ->
            screen.setCharacter(position.column + i, position.row + rows - 1, TextCharacter(c, GREEN, BLACK))
        }
        command.forEachIndexed { i, c ->
            screen.setCharacter(
                position.column + COMMAND_PREFIX.length + i,
                position.row + rows - 1, TextCharacter(c, WHITE, BLACK)
            )
        }
        screen.cursorPosition = TerminalPosition(
            position.column + min(size.columns - 2, 3 + command.length), position.row + rows - 1
        )

        screen.refresh()
        screen.doResizeIfNecessary()
    }

    private fun List<List<TextWithColor>>.buildColorStringArrays(): ScreenBuffer =
        map { line ->
            var idx = 0
            var nlCnt = 0
            line.map { it to it.text(MessageScope { onBufferChanged() }).toString() }
                .flatMap { (col, c) -> c.map { it1 -> it1 to col.color to col.background } }
                .replace('\t') { _, tabIdx -> (0..tabIdx % 4).joinToString("") { " " } }
                .remove('\b', 1)
                .groupBy { (c, _, _) ->
                    if (c == '\n') nlCnt++
                    this@LanternaConsole.size.columns.let {
                        if (it > 3) nlCnt + idx++ / (this@LanternaConsole.size.columns - 3) else 0
                    }
                }.values.toList()
                .mapIndexed { i, it -> (if (i == 0) ">: " else "   ").map { c -> c to YELLOW to BLACK } + it }
                .map { it.filterNot { it1 -> it1.first.isISOControl() } }
        }

    private suspend fun onNewCommand(command: String) {
        scrollIdx = 0
        onBufferChanged()
    }

    val scope: Scope
        get() {
            screen.startScreen()
            return Scope()
        }

    fun release() {
        cs.cancel()
        screen.stopScreen()
    }

    inner class Scope {

        fun out(vararg text: TextWithColor, autoRefresh: Boolean = true, newLine: Boolean = false) {
            var doUpdate = false
            buffer.put((buffer.lastOrNull()
                ?.nullWhen { newLine || it.last().newLine }
                ?.let { doUpdate = true; it + text.toList() } ?: text.toList())
                .mapIndexed { idx, t -> if (newLine && idx == text.lastIndex) t.copy(newLine = true) else t },
                doUpdate
            )
            if (autoRefresh) refresh()
        }

        fun out(msg: String, autoRefresh: Boolean = true, newLine: Boolean = false) =
            out(WHITE(msg), autoRefresh = autoRefresh, newLine = newLine)

        fun outNl(vararg text: TextWithColor, autoRefresh: Boolean = true) =
            out(*text, autoRefresh = autoRefresh, newLine = true)

        fun outNl(msg: String = "", autoRefresh: Boolean = true) = outNl(WHITE(msg), autoRefresh = autoRefresh)

        fun refresh() = onBufferChanged()

        fun release() = this@LanternaConsole.release()
    }
}

private fun List<Triple<Char, TextColor, TextColor>>.remove(
    from: Char,
    pre: Int = 0
): List<Triple<Char, TextColor, TextColor>> {
    val buffer = LinkedList<Triple<Char, TextColor, TextColor>>()
    return flatMap {
        buffer.add(it)
        when {
            it.first == from -> {
                buffer.clear()
                emptyList()
            }
            buffer.size == pre + 1 -> listOf(buffer.removeAt(0))
            else -> emptyList()
        }
    } + buffer
}

private fun List<Triple<Char, TextColor, TextColor>>.replace(
    from: Char,
    to: (Char, Int) -> String
): List<Triple<Char, TextColor, TextColor>> {
    var idx = 0
    return flatMap { value ->
        when (value.first) {
            from -> to(value.first, idx++).map { c -> c to value.second to value.third }
            else -> listOf(value)
        }
    }
}

inline fun LanternaConsole.scope(block: LanternaConsole.Scope.() -> Unit) = scope.block()

infix fun <A, B, C> Pair<A, B>.to(other: C): Triple<A, B, C> = Triple(first, second, other)


data class MessageScope(val refresh: () -> Unit)
typealias MessageBuilder = MessageScope.() -> Any
typealias Message = Triple<Boolean, Boolean, TextWithColor>

operator fun TextColor.invoke(bg: TextColor = BLACK, msg: MessageBuilder) = TextWithColor(msg, this, bg)
operator fun TextColor.invoke(msg: Any, bg: TextColor = BLACK) = TextWithColor({ msg }, this, bg)

data class TextWithColor(
    val text: MessageBuilder,
    val color: TextColor,
    val background: TextColor,
    val newLine: Boolean = false
)

operator fun TextWithColor.times(text: TextWithColor): List<TextWithColor> = listOf(this, text)
operator fun TextWithColor.times(text: String): List<TextWithColor> = listOf(this, WHITE(text))
operator fun TextWithColor.times(text: List<TextWithColor>): List<TextWithColor> = listOf(this, *text.toTypedArray())

operator fun String.times(text: TextWithColor): List<TextWithColor> = listOf(WHITE(this), text)
operator fun String.times(text: String): List<TextWithColor> = listOf(WHITE(this), WHITE(text))
operator fun String.times(text: List<TextWithColor>): List<TextWithColor> = listOf(WHITE(this), *text.toTypedArray())

operator fun List<TextWithColor>.times(text: TextWithColor): List<TextWithColor> = listOf(*toTypedArray(), text)
operator fun List<TextWithColor>.times(text: String): List<TextWithColor> = listOf(*toTypedArray(), WHITE(text))
operator fun List<TextWithColor>.times(text: List<TextWithColor>): List<TextWithColor> =
    listOf(*toTypedArray(), *text.toTypedArray())