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
import dev.zieger.utils.coroutines.channel.forEach
import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.misc.AntiSpamProxy
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class LanternaConsole(
    private val screen: Screen = DefaultTerminalFactory().createScreen(),
    val position: TerminalPosition = TOP_LEFT_CORNER,
    val preferredSize: TerminalSize? = null,
    val refreshInterval: IDurationEx? = 100.milliseconds,
    isStandalone: Boolean = true,
    private val bufferSize: Int = BUFFER_SIZE,
    private val cs: CoroutineScope = DefaultCoroutineScope(),
    private val hideCommandInput: Boolean = false
) {

    companion object : IScope {

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            LanternaConsole().scope {
                outnl("FooBooFo°\b\n°\boBooFooBooFoFoFooBFooFFoFooBoooBooFooBooooBooBoooFFooBooooBooFooBoooFoFooFooBooBooFFooBooooBoooBoooBoooBooFooBooFooBoo")
                outnl("mooo\t\tbooo")
                outnl(*arrayOf(WHITE("määäää")) + GREEN("foo", YELLOW), RED(WHITE) { "boo" }, BLUE("blub"))

                var startIdx = 0
                var printed = 0
                var job: Job? = null
                outnl(WHITE {
                    startIdx++
                    if (printed == 0) {
                        job = job ?: launchEx(interval = 1.seconds, delayed = 1.seconds) { printed++; refresh() }
                        " boofoo$printed "
                    } else "  DU HURENSOHN$printed  "
                })
                repeat(500) { out(WHITE { "${it + startIdx}|" }, autoRefresh = false) }
                outnl()
                refresh()

                outnl("hey")
                outnl("du")
                outnl("krasser")
                outnl("typ :-*")

                val progress = ConsoleProgressBar { refresh() }
                outnl(CYAN { if (progress.progressPercent == 1.0) remove(); "fooProg: " },
                    progress.textWithColor { if (it == 1.0) remove() })
                launchEx(interval = 1.milliseconds) { progress.progressPercent += 0.0001 }
            }
        }.asUnit()

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

    private var buffer = ArrayList<List<TextWithColor>>(bufferSize)
    private var bufferLines = 0
    private var scrollIdx = 0
    private var commandInput = ""
    private lateinit var lastSize: TerminalSize

    private val messageChannel = Channel<Message>(Channel.UNLIMITED)
    private val antiSpamProxy = AntiSpamProxy(100.milliseconds, cs)
    private val singleDispatcher = newSingleThreadContext("")

    private val graphicJobs = ArrayList<Job>()

    private var graphicsSet by OnChanged(false) {
        when (it) {
            true -> {
                refreshInterval?.also { interval ->
                    graphicJobs += cs.launchEx(interval = interval) { onBufferChanged() }
                }
                graphicJobs += cs.launchEx {
                    while (isActive) onKeyPressed(executeNativeBlocking { screen.readInput() })
                }
                graphicJobs += cs.launchEx(interval = 100.milliseconds) {
                    checkSize()
                }
                graphicJobs += cs.launchEx {
                    messageChannel.forEach { (text, autoRefresh, newLine, offset) ->
                        withContext(singleDispatcher) {
                            var doUpdate = false
                            val value = (buffer.lastOrNull()
                                ?.nullWhen { n -> offset > 0 || newLine || n.last().newLine }
                                ?.let { l -> doUpdate = true; l + text.toList() } ?: text.toList())
                                .mapIndexed { idx, t -> if (newLine && idx == text.lastIndex) t.copy(newLine = true) else t }

                            if (offset > 0) buffer.add(buffer.lastIndex - 1, value)
                            else if (doUpdate) buffer[buffer.lastIndex] = value
                            else buffer.add(value)
                            while (buffer.size >= bufferSize) buffer.removeAt(0)
                        }
                        if (autoRefresh) onBufferChanged()
                    }
                }
            }
            false -> graphicJobs.runEach { cancel() }
        }
    }

    init {
        if (isStandalone)
            draw(screen.newTextGraphics())
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
            launchEx { onNewCommand(newCommand) }.asUnit()
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
        antiSpamProxy {
            withContext(singleDispatcher) {
                screen.clear()
                printOutput()
                printCommandInput()
                screen.refresh()
                screen.doResizeIfNecessary()
            }
        }
    }

    private fun printOutput() {
        var nlIdx = 0
        val buffCopy = ArrayList(buffer)
        val stringArray = buffCopy.buildColorStringArrays {
            buffer = ArrayList(buffCopy.map { m -> m.filterNot { f -> f == it } })
            onBufferChanged()
        }
        val rows = size.rows
        bufferLines = stringArray.sumBy { it.size }
        stringArray.reversed().forEach { messages ->
            messages.reversed().forEach { m ->
                var columnIdx = 0
                val lineIdx = min(bufferLines - 1, rows - if (hideCommandInput) 1 else 2) - nlIdx++ + scrollIdx
                if (lineIdx in 0..rows - if (hideCommandInput) 1 else 2) m.forEach { (c, col, back) ->
                    val column = position.column + columnIdx++
                    val row = position.row + lineIdx
                    graphics.setCharacter(column, row, TextCharacter(c, col, back))
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

    private val lastMessageBuffer = HashMap<MessageId, String>()

    private fun List<List<TextWithColor>>.buildColorStringArrays(remove: (TextWithColor) -> Unit): ScreenBuffer =
        map { line ->
            var idx = 0
            var nlCnt = 0
            line.map {
                val scope =
                    MessageScope({ onBufferChanged() }, { remove(it) }, { it.active = false }, { it.active = true })
                val wasActive = it.active
                val message = it.text(scope).toString()
                it to when {
                    it.active && wasActive || wasActive -> message.also { s -> lastMessageBuffer[it.id] = s }
                    else -> lastMessageBuffer[it.id] ?: ""
                }
            }.flatMap { (col, c) -> c.map { m -> m to col.color() to col.background() } }
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

        override fun out(vararg text: TextWithColor, autoRefresh: Boolean, newLine: Boolean, offset: Int): () -> Unit {
            var doUpdate = false
            val value = (buffer.lastOrNull()
                ?.nullWhen { newLine || it.last().newLine }
                ?.let { doUpdate = true; it + text.toList() } ?: text.toList())
                .mapIndexed { idx, t -> if (newLine && idx == text.lastIndex) t.copy(newLine = true) else t }
            if (doUpdate) buffer[buffer.lastIndex] = value
            else buffer.add(value)
            if (autoRefresh) refresh()
            return { buffer.remove(value) }
        }

        override fun out(msg: String, autoRefresh: Boolean, newLine: Boolean, offset: Int): () -> Unit =
            out(WHITE(msg), autoRefresh = autoRefresh, newLine = newLine, offset = offset)

        override fun outnl(vararg text: TextWithColor, autoRefresh: Boolean, offset: Int): () -> Unit =
            out(*text, autoRefresh = autoRefresh, newLine = true, offset = offset)

        override fun outnl(msg: String, autoRefresh: Boolean, offset: Int): () -> Unit =
            outnl(WHITE(msg), autoRefresh = autoRefresh, offset = offset)

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

    fun out(msg: String, autoRefresh: Boolean = true, newLine: Boolean = false, offset: Int = 0): () -> Unit
    fun outnl(vararg text: TextWithColor, autoRefresh: Boolean = true, offset: Int = 0): () -> Unit
    fun outnl(msg: String = "", autoRefresh: Boolean = true, offset: Int = 0): () -> Unit

    fun refresh()
    fun release()
}

private fun List<MessageChar>.remove(
    from: Char,
    pre: Int = 0
): List<MessageChar> {
    val buffer = LinkedList<MessageChar>()
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

private fun List<MessageChar>.replace(
    from: Char,
    to: (Char, Int) -> String
): List<MessageChar> {
    var idx = 0
    return flatMap { value ->
        when (value.first) {
            from -> to(value.first, idx++).map { c -> c to value.second to value.third }
            else -> listOf(value)
        }
    }
}

inline fun <T> LanternaConsole.scope(block: LanternaConsole.Scope.() -> T): T = scope.block()

infix fun <A, B, C> Pair<A, B>.to(other: C): Triple<A, B, C> = Triple(first, second, other)


data class MessageScope(
    val refresh: () -> Unit,
    val remove: () -> Unit,
    val pause: () -> Unit,
    val resume: () -> Unit
)
typealias MessageId = Long
typealias MessageBuilder = MessageScope.() -> Any
typealias MessageChar = Triple<Char, TextColor, TextColor>
typealias ScreenBuffer = List<List<List<MessageChar>>>

data class Message(
    val message: List<TextWithColor>,
    val autoRefresh: Boolean,
    val newLine: Boolean,
    val offset: Int
)

operator fun TextColor.invoke(bg: TextColor = BLACK, msg: MessageBuilder) = TextWithColor(msg, this, bg)
operator fun TextColor.invoke(msg: Any, bg: TextColor = BLACK) = TextWithColor({ msg }, this, bg)

data class TextWithColor(
    val text: MessageBuilder,
    val color: () -> TextColor,
    val background: () -> TextColor,
    val newLine: Boolean = false,
    var active: Boolean = true
) {

    companion object {

        private val lastId = AtomicLong(0)

        val newId: MessageId get() = lastId.getAndIncrement()
    }

    constructor(
        text: MessageBuilder,
        color: TextColor,
        background: TextColor,
        newLine: Boolean = false
    ) : this(text, { color }, { background }, newLine)

    val id: MessageId = newId
}

operator fun TextWithColor.times(text: TextWithColor): List<TextWithColor> = listOf(this, text)
operator fun TextWithColor.times(text: String): List<TextWithColor> = listOf(this, WHITE(text))
operator fun TextWithColor.times(text: List<TextWithColor>): List<TextWithColor> =
    listOf(this, *text.toTypedArray())

operator fun String.times(text: TextWithColor): List<TextWithColor> = listOf(WHITE(this), text)
operator fun String.times(text: String): List<TextWithColor> = listOf(WHITE(this), WHITE(text))
operator fun String.times(text: List<TextWithColor>): List<TextWithColor> =
    listOf(WHITE(this), *text.toTypedArray())

operator fun List<TextWithColor>.times(text: TextWithColor): List<TextWithColor> =
    listOf(*toTypedArray(), text)

operator fun List<TextWithColor>.times(text: String): List<TextWithColor> =
    listOf(*toTypedArray(), WHITE(text))

operator fun List<TextWithColor>.times(text: List<TextWithColor>): List<TextWithColor> =
    listOf(*toTypedArray(), *text.toTypedArray())
