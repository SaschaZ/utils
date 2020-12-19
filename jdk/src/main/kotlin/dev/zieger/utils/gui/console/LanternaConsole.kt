@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.gui.console.MessageBuffer.Companion.SPAM_DURATION
import dev.zieger.utils.gui.console.ProgressEntity.*
import dev.zieger.utils.gui.console.ScreenBuffer.Companion.buffer
import dev.zieger.utils.gui.console.ScreenLine.Companion.line
import dev.zieger.utils.gui.console.ScreenLineGroup.Companion.group
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.*
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*


class LanternaConsole(
    private val screen: Screen = DefaultTerminalFactory().createScreen(),
    val position: TerminalPosition = TOP_LEFT_CORNER,
    val preferredSize: TerminalSize? = null,
    val refreshInterval: IDurationEx? = SPAM_DURATION,
    isStandalone: Boolean = true,
    private val bufferSize: Int = BUFFER_SIZE,
    private val cs: CoroutineScope = DefaultCoroutineScope(),
    private val hideCommandInput: Boolean = false
) {

    companion object : IScope {

        private const val BUFFER_SIZE = 4098
        private const val SCROLL_DELTA = 5
        private const val SCROLL_MAX = 1024
        private const val COMMAND_PREFIX = "\$: "

        internal var lastInstance: LanternaConsole? = null
            private set

        override fun out(vararg text: TextWithColor, autoRefresh: Boolean, newLine: Boolean, offset: Int): () -> Unit {
            return lastInstance?.scope?.out(*text, autoRefresh = autoRefresh, newLine = newLine, offset = offset)
                ?: throw IllegalStateException("Can not use global scope when no LanternaConsole instance was created yet.")
        }

        override fun out(msg: String, autoRefresh: Boolean, newLine: Boolean, offset: Int): () -> Unit {
            return lastInstance?.scope?.out(msg, autoRefresh = autoRefresh, newLine = newLine, offset = offset)
                ?: throw IllegalStateException("Can not use global scope when no LanternaConsole instance was created yet.")
        }

        override fun outnl(vararg text: TextWithColor, autoRefresh: Boolean, offset: Int): () -> Unit {
            return lastInstance?.scope?.outnl(*text, autoRefresh = autoRefresh, offset = offset)
                ?: throw IllegalStateException("Can not use global scope when no LanternaConsole instance was created yet.")
        }

        override fun outnl(msg: String, autoRefresh: Boolean, offset: Int): () -> Unit {
            return lastInstance?.scope?.outnl(msg, autoRefresh = autoRefresh, offset = offset)
                ?: throw IllegalStateException("Can not use global scope when no LanternaConsole instance was created yet.")
        }

        override fun refresh() {
            lastInstance?.scope?.refresh()
                ?: throw IllegalStateException("Can not use global scope when no LanternaConsole instance was created yet.")
        }

        override fun release() {
            lastInstance?.scope?.release()
                ?: throw IllegalStateException("Can not use global scope when no LanternaConsole instance was created yet.")
        }
    }

    private lateinit var graphics: TextGraphics

    private val size: TerminalSize
        get() = preferredSize ?: graphics.size
    private lateinit var lastSize: TerminalSize

    private val buffer = MessageBuffer(cs, { size.columns }, spamDuration = refreshInterval ?: SPAM_DURATION) {
        onBufferChanged()
    }

    private var bufferLines = 0
    private var scrollIdx = 0
    private var lastScrollIdx: Int = scrollIdx
    private var commandInput = ""

    private var lastScreenBuffer: ScreenBuffer? = null

    private val graphicJobs = ArrayList<Job>()

    private var graphicsSet by OnChanged(false) {
        when (it) {
            true -> {
                refreshInterval?.also { interval ->
                    graphicJobs += cs.launchEx(interval = interval) { buffer.update() }
                }
                graphicJobs += cs.launchEx {
                    while (isActive) onKeyPressed(executeNativeBlocking { screen.readInput() })
                }
                graphicJobs += cs.launchEx(interval = 100.milliseconds) {
                    checkSize()
                }
            }
            false -> {
                graphicJobs.runEach { cancel() }
                graphicJobs.clear()
            }
        }
    }

    init {
        if (isStandalone)
            draw(screen.newTextGraphics())
        if (hideCommandInput) screen.cursorPosition = null
        lastInstance = this
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
            Scope().outnl(newCommand)
            commandInput = ""
            onBufferChanged()
            onNewCommand(newCommand).asUnit()
        }
        else -> Unit
    }

    fun draw(graphics: TextGraphics) {
        this.graphics = graphics
        lastSize = graphics.size
        graphicsSet = true
        onBufferChanged()
    }

    private fun onBufferChanged() {
        if (!::graphics.isInitialized) return
        val screenBuffer = buffer.lastScreenBuffer ?: return

        val clear = lastScreenBuffer?.numLines?.let { it == screenBuffer.numLines } != true
                || scrollIdx != lastScrollIdx
        screen.draw(clear) {
            printOutput(screenBuffer - lastScreenBuffer)
            printCommandInput()
        }
        lastScreenBuffer = screenBuffer
        lastScrollIdx = scrollIdx
    }

    private val ScreenBuffer.numLines: Int get() = sumBy { it.size }

    private operator fun ScreenBuffer.minus(other: ScreenBuffer?): ScreenBuffer =
        if (other?.size != size) this else
            mapIndexed { index, group -> other.getOrNull(index)?.let { group - it } ?: group }.buffer

    private operator fun ScreenLineGroup.minus(other: ScreenLineGroup): ScreenLineGroup =
        if (other.size != size) this else
            mapIndexed { index, line -> other.getOrNull(index)?.let { line - it } ?: line }.group

    private operator fun ScreenLine.minus(other: ScreenLine): ScreenLine =
        (0..max(lastIndex, other.lastIndex)).map { index ->
            getOrNull(index)?.let { char ->
                other.getOrNull(index)?.let { char - it } ?: char
            } ?: ScreenChar(' ')
        }.line

    private operator fun ScreenChar?.minus(other: ScreenChar?): ScreenChar? = nullWhen { it == other }

    private fun Screen.draw(clear: Boolean, block: Screen.() -> Unit) {
        if (clear) clear()
        block()
        refresh()
        doResizeIfNecessary()
    }

    private fun printOutput(stringArray: ScreenBuffer) {
        var nlIdx = 0
        val rows = size.rows
        bufferLines = stringArray.sumBy { it.size }
        stringArray.reversed().forEach { messages ->
            messages.reversed().forEach { m ->
                var columnIdx = 0
                val lineIdx = min(bufferLines - 1, rows - if (hideCommandInput) 1 else 2) - nlIdx++ + scrollIdx
                if (lineIdx in 0..rows - if (hideCommandInput) 1 else 2) m.forEach { sc ->
                    sc?.let { (c, col, back) ->
                        val column = position.column + columnIdx++
                        val row = position.row + lineIdx
                        graphics.setCharacter(
                            column, row, TextCharacter(
                                c,
                                col ?: graphics.foregroundColor,
                                back ?: graphics.backgroundColor
                            )
                        )
                    }
                }
            }
        }
    }

    private fun printCommandInput() {
        if (hideCommandInput) return

        val rows = size.rows
        val n = size.columns - COMMAND_PREFIX.length
        val command = commandInput.takeLast(n - 2).let {
            it.let { s -> if (s.length == n - 2) "…${s.takeLast(s.length - 1)}" else s } +
                    (0..max(0, n - it.length)).joinToString("") { " " }
        }
        val row = position.row + min(bufferLines, rows - 1)
        COMMAND_PREFIX.forEachIndexed { i, c ->
            val column = position.column + i
            graphics.setCharacter(column, row, TextCharacter(c, BLACK, TextColor.Indexed.fromRGB(255, 128, 0)))
        }
        command.forEachIndexed { i, c ->
            val column = position.column + COMMAND_PREFIX.length + i
            graphics.setCharacter(column, row, TextCharacter(c, BLACK, TextColor.Indexed.fromRGB(255, 128, 0)))
        }
        screen.cursorPosition = TerminalPosition(
            position.column + min(size.columns - 2, 3 + commandInput.length), row
        )
    }

    private suspend fun onNewCommand(command: String) {
        scrollIdx = 0
        onBufferChanged()
    }

    private var started by OnChanged(false) {
        when (it) {
            true -> screen.startScreen()
            false -> screen.stopScreen()
        }
    }

    val scope: Scope
        get() {
            started = true
            return Scope()
        }

    fun release() {
        cs.cancel()
        started = false
    }

    inner class Scope : IScope {

        override fun out(vararg text: TextWithColor, autoRefresh: Boolean, newLine: Boolean, offset: Int): () -> Unit =
            buffer.addMessage(Message(text.toList(), autoRefresh, newLine, offset))

        override fun refresh() = onBufferChanged()

        override fun release() = this@LanternaConsole.release()
    }
}

interface IScope {

    fun out(
        vararg text: TextWithColor,
        autoRefresh: Boolean = true,
        newLine: Boolean = false,
        offset: Int = 0
    ): () -> Unit

    fun out(
        text: List<TextWithColor>,
        autoRefresh: Boolean = true,
        newLine: Boolean = false,
        offset: Int = 0
    ): () -> Unit = out(*text.toTypedArray(), autoRefresh = autoRefresh, newLine = newLine, offset = offset)

    fun out(msg: String, autoRefresh: Boolean = true, newLine: Boolean = false, offset: Int = 0): () -> Unit =
        out(text({ msg }), autoRefresh = autoRefresh, newLine = newLine, offset = offset)


    fun outnl(vararg text: TextWithColor, autoRefresh: Boolean = true, offset: Int = 0): () -> Unit =
        out(*text, autoRefresh = autoRefresh, newLine = true, offset = offset)

    fun outnl(text: List<TextWithColor>, autoRefresh: Boolean = true, offset: Int = 0): () -> Unit =
        out(text, autoRefresh = autoRefresh, newLine = true, offset = offset)

    fun outnl(msg: String = "", autoRefresh: Boolean = true, offset: Int = 0): () -> Unit =
        outnl(text({ msg }), autoRefresh = autoRefresh, offset = offset)

    fun refresh()
    fun release()
}

inline fun <T> LanternaConsole.scope(block: LanternaConsole.Scope.() -> T): T = scope.block()

infix fun <A, B, C> Pair<A, B>.to(third: C): Triple<A, B, C> = Triple(first, second, third)

