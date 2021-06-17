@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbstractComponent
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*

class ConsoleComponent : AbstractComponent<ConsoleComponent>(), FocusableConsoleComponent {

    override var hasFocus: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    override var scope: CoroutineScope? = null
        set(value) {
            field = value
            value?.also { scope ->
                scope.launch {
                    while (isActive) {
                        checkSize()
                        delay(100)
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
                        scrollToBottom()
                    }
                }
            }
        }
    override lateinit var options: ConsoleOptions
    private val rawBuffer = LinkedList<TextString>()
    internal var buffer = LinkedList<List<TextString>>()
        private set
    private val bufferChannel = Channel<Pair<Boolean, TextString>>(Channel.UNLIMITED)
    private val onBufferChangedDispatcher = newSingleThreadContext("onBufferChanged")

    private var lastSize: TerminalSize? = null
    internal var scrollIdx = 0
        private set

    override fun createDefaultRenderer() = ConsoleRenderer()

    override fun textCharacter(character: Char): TextCharacterWrapper =
        TextCharacterWrapper(character, foreground = options.foreground, background = options.background)

    private fun onBufferChanged() {
        val preBuffer = LinkedList<List<TextString>>()
        rawBuffer.forEachIndexed { row, str ->
            preBuffer.add(row, str()
                .addSpaceForEmptyString()
                .processBackslashN()
                .flatMapIndexed { idx: Int, chars: List<TextChar> ->
                    chars.processBackslashT()
                        .filterUnwantedWhitespaces()
                        .wrapLines()
                        .addPrefix(idx)
                }.map { { it.toTypedArray() } })
        }
        buffer = preBuffer
        invalidate()
    }

    private fun Array<TextChar>.addSpaceForEmptyString(): Array<TextChar> =
        if (isEmpty()) arrayOf({ textCharacter(' ') }) else this

    private fun Array<TextChar>.processBackslashN(): Collection<List<TextChar>> {
        var newLine = 0
        return groupBy {
            when (it().character) {
                '\n' -> newLine++
                else -> newLine
            }
        }.values
    }

    private fun List<TextChar>.processBackslashT(): List<TextChar> =
        flatMapIndexed { idx: Int, char: TextChar ->
            when (char().character) {
                '\t' -> (0..(3 - idx % 4).let { if (it == 0) 3 else it }).map { { textCharacter(' ') } }
                else -> listOf(char)
            }
        }

    private fun List<TextChar>.wrapLines(): Collection<List<TextChar>> =
        groupByIndexed { (idx, _) ->
            val cols = this@ConsoleComponent.size.columns
            when {
                cols - options.commandPrefix().size != 0 -> idx / (cols - options.commandPrefix().size)
                else -> 0
            }
        }.values

    private fun Collection<List<TextChar>>.addPrefix(idx: Int): Collection<List<TextChar>> {
        val commandPrefix = options.commandPrefix()
        val prefixNewLine = options.commandNewLinePrefix()
        return mapIndexed { index, value ->
            val prefix = if (index == 0 && idx == 0) commandPrefix else prefixNewLine
            listOf(*prefix, *value.toTypedArray())
        }
    }

    private fun List<TextChar>.filterUnwantedWhitespaces(): List<TextChar> =
        filterNot { it().character.let { it.isWhitespace() && it != ' ' } }

    private suspend fun checkSize() {
        val prevSize = lastSize
        val termSize = textGUI?.screen?.terminalSize
        lastSize = termSize ?: lastSize
        preferredSize = lastSize
        if (prevSize != lastSize)
            withContext(onBufferChangedDispatcher) {
                onBufferChanged()
            }
    }

    private suspend fun scroll(delta: Int) {
        withContext(onBufferChangedDispatcher) {
            val bufferLines = buffer.sumOf { it.size }
            val lowerBound =
                (-bufferLines.coerceAtLeast(position.row) + size.rows - position.row).coerceAtMost(-position.row)
            val upperBound = -position.row
            scrollIdx = (scrollIdx + delta).coerceIn(lowerBound..upperBound)

            onBufferChanged()
        }
    }

    private suspend fun scrollToTop() = scroll(buffer.sumOf { it.size })

    private suspend fun scrollToBottom() = scroll(-buffer.sumOf { it.size })

    override suspend fun onKeyPressed(keyStroke: KeyStroke) {
        println("onKeyPressed($keyStroke)")
        when (keyStroke.keyType) {
            KeyType.ArrowUp -> when {
                keyStroke.isAltDown && !keyStroke.isShiftDown -> scroll(10)
                keyStroke.isShiftDown && !keyStroke.isAltDown -> scroll(20)
                keyStroke.isShiftDown && keyStroke.isAltDown -> scrollToTop()
                else -> scroll(1)
            }
            KeyType.ArrowDown -> when {
                keyStroke.isAltDown && !keyStroke.isShiftDown -> scroll(-10)
                keyStroke.isShiftDown && !keyStroke.isAltDown -> scroll(-20)
                keyStroke.isShiftDown && keyStroke.isAltDown -> scrollToBottom()
                else -> scroll(-1)
            }
            else -> Unit
        }
    }

    override fun out(str: TextString) {
        bufferChannel.offer(false to str)
    }

    override fun outNl(str: TextString) {
        bufferChannel.offer(true to str)
    }
}

private inline fun <T, K> Collection<T>.groupByIndexed(keySelector: (Pair<Int, T>) -> K): Map<K, List<T>> {
    var idx = 0
    return groupBy { keySelector(idx++ to it) }
}