@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.log.console

import com.googlecode.lanterna.TerminalSize
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.lang.Integer.max
import java.lang.Integer.min

data class MessageScope(val refresh: () -> Unit)
typealias Message = MessageScope.() -> Any
typealias MessageStore = Triple<Boolean, Boolean, Message>

class LanternaConsole(bufferSize: Int = BUFFER_SIZE) {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            LanternaConsole().scope {
                outNl("FooBooFo\noBooFooBooFoFoFooBFooFFoFooBoooBooFooBooooBooBoooFFooBooooBooFooBoooFoFooFooBooBooFFooBooooBoooBoooBoooBooFooBooFooBoo")
                out("mooobooo\n")
                out("määäää" * GREEN("foo", YELLOW) * RED("boo") * "blub")

                var startIdx = 0
                var printed = 0
                var job: Job? = null
                out {
                    startIdx++
                    if (printed == 0) {
                        job = job ?: launchEx(interval = 2.seconds, delayed = 2.seconds) { printed++; refresh() }
                        " boofoo$printed "
                    } else "  DU HURENSOHN$printed  "
                }
                repeat(500) { out(false) { "${it + startIdx}|" } }
                refresh()
                outNl("hey")
                outNl("du")
                outNl("krasser")
                outNl("typ :-*")
                delay(1.minutes)
            }
        }.asUnit()

        private const val BUFFER_SIZE = 128
        private const val SCROLL_DELTA = 5
        private const val SCROLL_MAX = 1024
        private const val COMMAND_PREFIX = "\$: "
    }

    private val cs = DefaultCoroutineScope()
    private val screen: Screen = DefaultTerminalFactory().createScreen()

    private val buffer = FiFo<MessageStore>(bufferSize)
    private var bufferLines = 0
    private var bottomIdx = 0
    private var commandInput = COMMAND_PREFIX
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
            delta >= 0 -> min(delta, max(0, bufferLines - screen.terminalSize.rows - bottomIdx + 1))
            else -> max(-bottomIdx, delta)
        }
        screen.scrollLines(0, SCROLL_MAX, newDelta)
        bottomIdx += newDelta
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
        Backspace -> if (commandInput.length > 3) {
            commandInput = commandInput.take(commandInput.length - 1)
            onBufferChanged()
        } else Unit
        Enter -> {
            val newCommand = commandInput.removePrefix(COMMAND_PREFIX)
            Scope().outNl(newCommand)
            commandInput = COMMAND_PREFIX
            onBufferChanged()
            launchEx { onNewCommand(newCommand) }.asUnit()
        }
        else -> Unit
    }

    private fun onBufferChanged() {
        screen.clear()
        screen.newTextGraphics().run {
            var nlIdx = 1
            buffer.reversed().forEach { (_, _, message) ->
                nlIdx = message(MessageScope { onBufferChanged() }).processMessage(nlIdx)
            }
            bufferLines = nlIdx
            putString(0, screen.terminalSize.rows - 1, commandInput)
        }
        screen.refresh()
        screen.doResizeIfNecessary()
    }

    private fun Any.processMessage(idx: Int): Int {
        var nlIdx = idx
        when (this@processMessage) {
            is List<*> -> forEach {
                nlIdx = it?.processMessage(nlIdx) ?: nlIdx
            }
            is TextWithColor -> text(MessageScope { onBufferChanged() }).put(nlIdx, color, background)
            else -> nlIdx = put(nlIdx)
        }
        return nlIdx
    }

    private fun Any.put(idx: Int, color: TextColor = WHITE, background: TextColor = BLACK): Int =
        screen.newTextGraphics().let { g ->
            g.foregroundColor = color
            g.backgroundColor = background

            var nlIdx = idx
            toString().split("\n")
                .flatMap {
                    if (it.isEmpty()) listOf(" ") else it.chunkedSequence(screen.terminalSize.columns - 3).toList()
                }
                .mapIndexed { idx, s -> if (idx == 0) ">: $s" else "   $s" }
                .reversed()
                .forEach {
                    val lineIdx = screen.terminalSize.rows - nlIdx++ + bottomIdx
                    if (lineIdx < screen.terminalSize.rows) g.putString(
                        0, lineIdx - 2, it.filterNot { c -> c.isISOControl() }.trim()
                    )
                }
            nlIdx
        }

    private suspend fun onNewCommand(command: String) {
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

        fun out(autoRefresh: Boolean = true, msg: Message) {
            val last = buffer.lastOrNull().nullWhen { it.second }
            buffer.put(last.concat(autoRefresh, false, msg), last != null)
            if (autoRefresh) onBufferChanged()
        }

        fun out(msg: Any, autoRefresh: Boolean = true) = out(autoRefresh) { msg }

        fun outNl(autoRefresh: Boolean = true, msg: Message) {
            buffer.put(autoRefresh to true to msg)
            if (autoRefresh) onBufferChanged()
        }

        fun outNl(msg: Any, autoRefresh: Boolean = true) = outNl(autoRefresh) { msg }

        private fun MessageStore?.concat(
            aR: Boolean,
            wNl: Boolean,
            msg: Message
        ): MessageStore = this?.let { (autoRefresh, withNl, message) ->
            val newMsg: Message = { "${message(this)}${msg()}" }
            (autoRefresh || aR) to (withNl || wNl) to newMsg
        } ?: (aR to wNl to msg)

        fun refresh() = onBufferChanged()

        fun release() = this@LanternaConsole.release()
    }
}

inline fun LanternaConsole.scope(block: LanternaConsole.Scope.() -> Unit) = scope.block()

infix fun <A, B, C> Pair<A, B>.to(other: C): Triple<A, B, C> = Triple(first, second, other)

operator fun TextColor.invoke(msg: Any, bg: TextColor = BLACK) = TextWithColor({ msg }, this, bg)

data class TextWithColor(
    val text: Message,
    val color: TextColor,
    val background: TextColor
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