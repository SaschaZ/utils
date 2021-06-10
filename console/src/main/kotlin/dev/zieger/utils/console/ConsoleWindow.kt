@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*

typealias TextChar = () -> TextCharacter
typealias TextString = () -> Array<TextChar>

class ConsoleComponent(scope: CoroutineScope) : AbstractComponent<ConsoleComponent>() {

    internal val rawBuffer = LinkedList<TextString>()
    internal var buffer = LinkedList<List<TextString>>()
        private set
    private val bufferChannel = Channel<Pair<Boolean, TextString>>(1024)
    private val onBufferChangedDispatcher = newSingleThreadContext("onBufferChanged")

    private var lastSize: TerminalSize? = null
    internal var scrollIdx = 0
        private set

    init {
        scope.launch {
            while (isActive) {
                checkSize()
                delay(100)
            }
        }
        scope.launch {
            while (isActive) {
                suspendCancellableCoroutine<KeyStroke?> {
                    it.resume(textGUI?.screen?.readInput()) { t ->
                        System.err.println(t)
                    }
                }?.let { onKeyPressed(it) }
            }
        }
        scope.launch {
            for ((withNewLine, str) in bufferChannel) {
                withContext(onBufferChangedDispatcher) {
                    when (withNewLine) {
                        true -> rawBuffer.add(str)
                        false -> {
                            val existing = rawBuffer[rawBuffer.lastIndex]
                            rawBuffer[rawBuffer.lastIndex] = { existing() + str() }
                        }
                    }
                    onBufferChanged()
                }
            }
        }
    }

    override fun createDefaultRenderer() = ConsoleRenderer()

    private fun onBufferChanged() {
        val preBuffer = LinkedList<List<TextString>>()
        rawBuffer.forEachIndexed { row, str ->
            preBuffer.add(row, str().groupByIndexed { (idx, _) ->
                val cols = size.columns
                when {
                    cols != 0 -> idx / cols
                    else -> 0
                }
            }.values.map { { it.toTypedArray() } })
        }
        buffer = preBuffer
        invalidate()
    }

    private suspend fun checkSize() {
        val prevSize = lastSize
        lastSize = textGUI?.screen?.doResizeIfNecessary() ?: lastSize
        if (prevSize != lastSize)
            withContext(onBufferChangedDispatcher) {
                onBufferChanged()
            }
    }

    private suspend fun scroll(delta: Int) {
        withContext(onBufferChangedDispatcher) {
            val bufferLines = buffer.sumOf { it.size }
            val newDelta = delta.coerceIn(
                (-scrollIdx - bufferLines + size.rows).coerceAtMost(0)..
                        (-scrollIdx).coerceAtLeast(0)
            )
            println("delta=$delta; newDelta=$newDelta; scrollIdx=${scrollIdx}; bufferLines=$bufferLines; sizeRows=${size.rows}")
            scrollIdx += newDelta

            onBufferChanged()
        }
    }

    private suspend fun onKeyPressed(keyStroke: KeyStroke) {
        println("onKeyPressed($keyStroke)")
        when (keyStroke.keyType) {
            KeyType.ArrowUp -> scroll(1)
            KeyType.ArrowDown -> scroll(-1)
            else -> Unit
        }
    }

    fun out(str: TextString) =
        bufferChannel.offer(false to str)

    fun outNl(str: TextString) =
        bufferChannel.offer(true to str)
}

inline fun <T, K> Array<T>.groupByIndexed(keySelector: (Pair<Int, T>) -> K): Map<K, List<T>> {
    var idx = 0
    return groupBy { keySelector(idx++ to it) }
}

class ConsoleRenderer : ComponentRenderer<ConsoleComponent> {

    override fun getPreferredSize(component: ConsoleComponent): TerminalSize {
        val buffer = LinkedList(component.buffer)
        return TerminalSize(buffer.maxOfOrNull { it.size } ?: 1, buffer.size)
    }

    override fun drawComponent(graphics: TextGUIGraphics, component: ConsoleComponent) {
        var bufferLine = 0
        graphics.apply {
            LinkedList(component.buffer).forEachIndexed { _, charBlock ->
                charBlock.forEachIndexed { _, chars ->
                    chars().forEachIndexed { col, char ->
                        setCharacter(
                            component.position.column + col,
                            component.position.row + bufferLine + component.scrollIdx,
                            char()
                        )
                    }
                    bufferLine++
                }
            }
        }
    }
}

class ConsoleWindow(
    title: String = "",
    position: TerminalPosition? = null,
    size: TerminalSize? = null,
    private val window: Window = BasicWindow(title)
) : Window by window {

    private val consoleComponent = ConsoleComponent(CoroutineScope(Dispatchers.IO))

    init {
        position?.let { window.position = it }
        size?.let { setFixedSize(it) }
        window.decoratedSize = TerminalSize.ZERO
        window.theme = SimpleTheme.makeTheme(
            true,
            TextColor.ANSI.WHITE,
            TextColor.ANSI.BLACK,
            TextColor.ANSI.BLUE,
            TextColor.ANSI.BLACK_BRIGHT,
            TextColor.ANSI.WHITE,
            TextColor.ANSI.BLUE,
            TextColor.ANSI.BLACK
        )
        window.setHints(
            listOf(
                Window.Hint.NO_DECORATIONS,
                Window.Hint.NO_POST_RENDERING,
                Window.Hint.FIT_TERMINAL_WINDOW,
                Window.Hint.FULL_SCREEN
            )
        )
        component = consoleComponent
    }

    fun out(str: TextString) = consoleComponent.out(str)

    fun outNl(str: TextString) = consoleComponent.outNl(str)
}

class Console(
    vararg windows: ConsoleWindow = arrayOf(ConsoleWindow()),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    block: suspend ConsoleWindow.() -> Unit
) {

    private val screen: Screen = DefaultTerminalFactory().createScreen()
    private val windowManager = MultiWindowTextGUI(screen)

    init {
        windowManager.run {
            screen.startScreen()
            windows.forEach { addWindow(it) }
        }
        scope.launch { block(windows.last()) }
        windows.forEach { windowManager.waitForWindowToClose(it) }
    }
}

operator fun (() -> String).unaryPlus(): TextString =
    { this().map { { TextCharacter(it) } }.toTypedArray() }

operator fun String.unaryPlus(): TextString =
    { map { { TextCharacter(it) } }.toTypedArray() }

operator fun TextString.times(foreground: TextColor): TextString =
    { this().map { { it().run { TextCharacter(character, foreground, backgroundColor, modifiers) } } }.toTypedArray() }

operator fun TextString.times(foreground: () -> TextColor): TextString =
    { this().map { { it().run { TextCharacter(character, foreground(), backgroundColor, modifiers) } } }.toTypedArray() }

operator fun TextString.div(background: TextColor): TextString =
    { this().map { { it().run { TextCharacter(character, foregroundColor, background, modifiers) } } }.toTypedArray() }

operator fun TextString.div(background: () -> TextColor): TextString =
    { this().map { { it().run { TextCharacter(character, foregroundColor, background(), modifiers) } } }.toTypedArray() }