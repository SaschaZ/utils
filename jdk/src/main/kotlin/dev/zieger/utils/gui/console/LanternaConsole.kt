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
import dev.zieger.utils.misc.*
import dev.zieger.utils.time.milliseconds
import dev.zieger.utils.time.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import kotlin.collections.lastOrNull

class LanternaConsole(
    private val screen: Screen = DefaultTerminalFactory().createScreen(),
    val position: TerminalPosition = TOP_LEFT_CORNER,
    val preferredSize: TerminalSize? = null,
    isStandalone: Boolean = true,
    bufferSize: Int = BUFFER_SIZE,
    private val cs: CoroutineScope = DefaultCoroutineScope()
) {

    companion object {

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
    }

    private lateinit var graphics: TextGraphics

    private val size: TerminalSize
        get() = preferredSize ?: graphics.size

    private var buffer = FiFo<List<TextWithColor>>(bufferSize)
    private var bufferLines = 0
    private var scrollIdx = 0
    private var commandInput = ""
    private lateinit var lastSize: TerminalSize

    private val messageChannel = Channel<Triple<List<TextWithColor>, Boolean, Boolean>>(Channel.UNLIMITED)
    private val antiSpamProxy = AntiSpamProxy(100.milliseconds, cs)
    private val singleDispatcher = newSingleThreadContext("")

    private val graphicJobs = ArrayList<Job>()

    private var graphicsSet by OnChanged(false) {
        when (it) {
            true -> {
                graphicJobs += cs.launchEx {
                    while (isActive) onKeyPressed(executeNativeBlocking { screen.readInput() })
                }
                graphicJobs += cs.launchEx(interval = 100.milliseconds) {
                    checkSize()
                }
                graphicJobs += cs.launchEx {
                    messageChannel.forEach { (text, autoRefresh, newLine) ->
                        withContext(singleDispatcher) {
                            var doUpdate = false
                            buffer.put((buffer.lastOrNull()
                                ?.nullWhen { newLine || it.last().newLine }
                                ?.let { doUpdate = true; it + text.toList() } ?: text.toList())
                                .mapIndexed { idx, t -> if (newLine && idx == text.lastIndex) t.copy(newLine = true) else t },
                                doUpdate
                            )
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
        val stringArray = buffer.buildColorStringArrays {
            buffer = FiFo(BUFFER_SIZE, buffer.map { m -> m.filterNot { f -> f == it } })
            onBufferChanged()
        }
        val rows = size.rows
        stringArray.reversed().forEach { messages ->
            messages.reversed().forEach { m ->
                var columnIdx = 0
                val lineIdx = rows - nlIdx++ + scrollIdx - 3
                if (lineIdx in 0..rows - 3) m.forEach { (c, col, back) ->
                    val column = position.column + columnIdx++
                    val row = position.row + lineIdx
                    graphics.setCharacter(column, row, TextCharacter(c, col, back))
                }
            }
        }
        bufferLines = nlIdx
    }

    private fun printCommandInput() {
        val rows = size.rows
        val n = size.columns - COMMAND_PREFIX.length
        val command = commandInput.takeLast(n - 2).let {
            it + (0..max(0, n - it.length)).joinToString("") { " " }
        }
        COMMAND_PREFIX.forEachIndexed { i, c ->
            val row = position.row + rows - 1
            val column = position.column + i
            graphics.setCharacter(column, row, TextCharacter(c, WHITE, BLUE))
        }
        command.forEachIndexed { i, c ->
            val row = position.row + rows - 1
            val column = position.column + COMMAND_PREFIX.length + i
            graphics.setCharacter(column, row, TextCharacter(c, BLACK, CYAN))
        }
        screen.cursorPosition = TerminalPosition(
            position.column + min(size.columns - 2, 3 + command.length), position.row + rows - 1
        )
    }

    private fun List<List<TextWithColor>>.buildColorStringArrays(remove: (TextWithColor) -> Unit): ScreenBuffer =
        map { line ->
            var idx = 0
            var nlCnt = 0
            line.map { it to it.text(MessageScope({ onBufferChanged() }, { remove(it) })).toString() }
                .flatMap { (col, c) -> c.map { it1 -> it1 to col.color() to col.background() } }
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

    inner class Scope {

        fun out(vararg text: TextWithColor, autoRefresh: Boolean = true, newLine: Boolean = false) {
            messageChannel.offer(text.toList() to autoRefresh to newLine)
        }

        fun out(msg: String, autoRefresh: Boolean = true, newLine: Boolean = false) =
            out(WHITE(msg), autoRefresh = autoRefresh, newLine = newLine)

        fun outnl(vararg text: TextWithColor, autoRefresh: Boolean = true) =
            out(*text, autoRefresh = autoRefresh, newLine = true)

        fun outnl(msg: String = "", autoRefresh: Boolean = true) = outnl(WHITE(msg), autoRefresh = autoRefresh)

        fun refresh() = onBufferChanged()

        fun release() = this@LanternaConsole.release()
    }
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

inline fun LanternaConsole.scope(block: LanternaConsole.Scope.() -> Unit) = scope.block()

infix fun <A, B, C> Pair<A, B>.to(other: C): Triple<A, B, C> = Triple(first, second, other)


data class MessageScope(
    val refresh: () -> Unit,
    val remove: () -> Unit
)
typealias MessageBuilder = MessageScope.() -> Any
typealias MessageChar = Triple<Char, TextColor, TextColor>
typealias ScreenBuffer = List<List<List<MessageChar>>>

operator fun TextColor.invoke(bg: TextColor = BLACK, msg: MessageBuilder) = TextWithColor(msg, this, bg)
operator fun TextColor.invoke(msg: Any, bg: TextColor = BLACK) = TextWithColor({ msg }, this, bg)

data class TextWithColor(
    val text: MessageBuilder,
    val color: () -> TextColor,
    val background: () -> TextColor,
    val newLine: Boolean = false
) {
    constructor(
        text: MessageBuilder,
        color: TextColor,
        background: TextColor,
        newLine: Boolean = false
    ) : this(text, { color }, { background }, newLine)
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


open class ConsoleProgressBar(
    initial: Int = 0,
    private var max: Int = 100,
    private val size: Int = DEFAULT_SIZE,
    foreground: IProgressColorProvider = PROGRESS_COLORS,
    background: IProgressColorProvider = ColorGradient(0x000000),
    private val fraction: String = FRACTION_2,
    private val refresh: () -> Unit
) {

    companion object {
        const val DEFAULT_SIZE = 27

        const val FRACTION_10 = " ▏▎▍▌▋▊▉██"
        const val FRACTION_100 = " ⡀⡄⡆⡇⣇⣧⣷⣿░"
        const val FRACTION_2 = "▱▰"

        data class ProgressTestData(
            val progress: Int = 0,
            val max: Int = 100
        )

        val PROGRESS_COLORS = ColorGradient(
            0xFF0000,
            0xF40000,
            0xFF8000,
            0xFFAF00,
            0xFFF000,
            0xFFFF00,
            0xCEFF00,
            0x8EFF00,
            0x4FFF00,
            0x40FF00,
            0x00FF00
        )

        private fun buildProgress(progress: Double, size: Int, fraction: String): String {
            val progressPercent = progress.coerceIn(0.0..1.0)
            val progressStr = " %.1f%% ".format(progressPercent * 100)
            val progressDigits = (size - 7).let { it + it % 2 }
            val done = progressPercent * progressDigits
            val doneInt = done.toInt()
            val fractionIdx = ((done - doneInt) * fraction.lastIndex).toInt()
                .coerceIn(0..fraction.lastIndex)
            return (0 until progressDigits).joinToString("") { idx ->
                when {
                    idx < doneInt || progressPercent == 1.0 -> "${fraction.last()}"
                    idx == doneInt -> "${fraction[fractionIdx]}"
                    else -> "${fraction.first()}"
                }
            }.run { substring(0 until length / 2) + progressStr + substring(length / 2 until length) }
        }
    }

    private var foregroundColor = foreground(0.0)
    private var backgroundColor = background(0.0)

    var progressPercent by OnChanged(
        initial / max.toDouble(),
        map = { it.coerceIn(0.0, 1.0) },
        notifyForInitial = true
    ) {
        foregroundColor = foreground(it)
        backgroundColor = background(it)
        text = buildProgress(it, size, fraction)
        refresh()
    }

    var progress by OnChanged(initial, notifyForInitial = true) {
        progressPercent = it / max.toDouble()
    }

    var text: String = buildProgress(progressPercent, size, FRACTION_2)
        private set

    val textWithColor get() = TextWithColor({ text }, { foregroundColor }, { backgroundColor })

    fun textWithColor(block: MessageScope.(Double) -> Unit = {}): TextWithColor =
        textWithColor.copy(text = {
            block(progressPercent)
            textWithColor.text(this)
        })
}

interface IProgressColorProvider : (Double) -> TextColor {

    override fun invoke(progress: Double): TextColor = colorFor(progress)

    fun colorFor(progress: Double): TextColor
}

class ColorGradient(vararg colors: Int) :
    IProgressColorProvider {
    private val colorList = colors.toList()
    private val singleColorRange = 1.0 / (colorList.size - 1)

    private fun Double.gradientItem(from: Int, to: Int): TextColor {
        fun Int.splitColors(): Triple<Int, Int, Int> {
            val r = ((this shr 16) and 0xFF).coerceIn(0..255)
            val g = ((this shr 8) and 0xFF).coerceIn(0..255)
            val b = (this and 0xFF).coerceIn(0..255)
            return Triple(r, g, b)
        }
        val (fr, fg, fb) = from.splitColors()
        val (tr, tg, tb) = to.splitColors()

        val r = (fr + (tr - fr) * this).toInt().coerceIn(0..255)
        val g = (fg + (tg - fg) * this).toInt().coerceIn(0..255)
        val b = (fb + (tb - fb) * this).toInt().coerceIn(0..255)
        return TextColor.RGB(r, g, b)
    }

    override fun colorFor(progress: Double): TextColor {
        if (colorList.isEmpty()) return TextColor.RGB(0, 0, 0)

        val colorIndex = (progress / singleColorRange).toInt().coerceIn(0..colorList.lastIndex)
        val colorProgress = progress % singleColorRange * (colorList.size - 1)
        return colorProgress.gradientItem(
            colorList[colorIndex],
            colorList[colorList.lastIndex.coerceAtMost(colorIndex + 1)]
        )
    }

    fun reversed() = ColorGradient(*colorList.reversed().toIntArray())
}