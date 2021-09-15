@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import dev.zieger.utils.console.ConsoleInstances.SIZE_SCOPE
import dev.zieger.utils.console.ConsoleInstances.UI_SCOPE
import dev.zieger.utils.koin.DI
import dev.zieger.utils.misc.nullWhen
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.koin.core.component.get
import java.util.*

class ConsoleComponent : AbstractFocusableComponent<ConsoleComponent>(), FocusableConsoleComponent {

    override var di: DI? = null
        set(value) {
            field = value
            uiScope = value?.get(UI_SCOPE)
            sizeScope = value?.get(SIZE_SCOPE)
            options = value?.get() ?: options
        }

    override var options: ConsoleOptions = ConsoleOptions()

    private var sizeScope: CoroutineScope? = null
        set(value) {
            field = value
            value?.launch {
                while (isActive) {
                    checkSize()
                    delay(100)
                }
            }
        }
    private var uiScope: CoroutineScope? = null
        set(value) {
            field = value
            value?.also { scope ->
                scope.launch {
                    for ((withNewLine, str) in bufferChannel) {
                        when (withNewLine) {
                            true -> rawBuffer.add(str)
                            false -> {
                                rawBuffer.nullWhen { it.isEmpty() }?.get(rawBuffer.lastIndex)?.let { existing ->
                                    rawBuffer[rawBuffer.lastIndex] = { existing() + str() }
                                } ?: run { rawBuffer.add(str) }
                            }
                        }
                        onBufferChanged()
                        scrollToBottom()
                    }
                }
            }
        }
    private val rawBuffer = LinkedList<TextString>()
    internal var buffer = LinkedList<List<TextString>>()
        private set
    private val bufferChannel = Channel<Pair<Boolean, TextString>>(Channel.UNLIMITED)

    private var lastSize: TerminalSize? = null
    internal var scrollIdx = 0
        private set

    override fun createDefaultRenderer() = ConsoleRenderer()

    private fun onBufferChanged() {
        val preBuffer = LinkedList<List<TextString>>()
        rawBuffer.setDefaultColors().forEachIndexed { row, str ->
            preBuffer.add(row, str()
                .processBackslashN()
                .addSpaceForEmptyString()
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

    private fun List<TextString>.setDefaultColors(): List<TextString> = map { str ->
        {
            str().map {
                {
                    it().run {
                        copy(
                            foreground = foreground.nullWhen { it == TextColor.ANSI.DEFAULT }
                                ?: options.foreground,
                            background = background.nullWhen { it == TextColor.ANSI.DEFAULT } ?: options.background
                        )
                    }
                }
            }.toTypedArray()
        }
    }

    private fun Array<TextChar>.processBackslashN(): Collection<List<TextChar>> {
        var newLine = 0
        return groupBy {
            when (it().character) {
                '\n' -> newLine++
                else -> newLine
            }
        }.values.map {
            it.map { it1 ->
                {
                    it1().let { it2 ->
                        if (it2.character == '\n' && it.size == 1)
                            TextCharacterWrapper(' ', options.foreground, options.background) else it2
                    }
                }
            }
        }
    }

    private fun Collection<List<TextChar>>.addSpaceForEmptyString(): Collection<List<TextChar>> =
        ifEmpty { listOf((+" " * options.foreground / options.background).invoke().toList()) }
            .map { it.ifEmpty { (+" " * options.foreground / options.background).invoke().toList() } }

    private fun List<TextChar>.processBackslashT(): List<TextChar> =
        flatMapIndexed { idx: Int, char: TextChar ->
            when (char().character) {
                '\t' -> (0..(3 - idx % 4).let { if (it == 0) 3 else it })
                    .map { { TextCharacterWrapper(' ', options.foreground, options.background) } }
                else -> listOf(char)
            }
        }

    private fun List<TextChar>.wrapLines(): Collection<List<TextChar>> =
        groupByIndexed { (idx, _) ->
            val cols = this@ConsoleComponent.size.columns
            when {
                cols - options.outputPrefix().size != 0 -> idx / (cols - options.outputPrefix().size)
                else -> 0
            }
        }.values

    private fun Collection<List<TextChar>>.addPrefix(idx: Int): Collection<List<TextChar>> {
        val commandPrefix = options.outputPrefix()
        val prefixNewLine = options.outputNewLinePrefix()
        return mapIndexed { index, value ->
            val prefix = if (index == 0 && idx == 0) commandPrefix else prefixNewLine
            listOf(*prefix, *value.toTypedArray())
        }
    }

    private fun List<TextChar>.filterUnwantedWhitespaces(): List<TextChar> =
        filterNot { it().character.let { it.isWhitespace() && it != ' ' } }

    private suspend fun checkSize() {
        textGUI?.screen?.terminalSize?.nullWhen { it == lastSize }
            ?.let { termSize ->
                lastSize = termSize
                preferredSize = termSize
                withContext(uiScope!!.coroutineContext) {
                    onBufferChanged()
                }
            }
    }

    private suspend fun scroll(delta: Int) {
        withContext(uiScope!!.coroutineContext) {
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

    override suspend fun handleKeyStroke(key: KeyStroke): Boolean {
        withContext(uiScope!!.coroutineContext) {
            when (key.keyType) {
                KeyType.ArrowUp -> when {
                    key.isAltDown && !key.isShiftDown -> scroll(10)
                    key.isShiftDown && !key.isAltDown -> scroll(20)
                    key.isShiftDown && key.isAltDown -> scrollToTop()
                    else -> scroll(1)
                }
                KeyType.ArrowDown -> when {
                    key.isAltDown && !key.isShiftDown -> scroll(-10)
                    key.isShiftDown && !key.isAltDown -> scroll(-20)
                    key.isShiftDown && key.isAltDown -> scrollToBottom()
                    else -> scroll(-1)
                }
                else -> Unit
            }
        }
        return true
    }

    override fun out(str: TextString) {
        bufferChannel.offer(false to str)
    }

    override fun outNl(str: TextString) {
        bufferChannel.offer(true to str)
    }

    override fun release() {
        di?.release()
    }
}

private inline fun <T, K> Collection<T>.groupByIndexed(keySelector: (Pair<Int, T>) -> K): Map<K, List<T>> {
    var idx = 0
    return groupBy { keySelector(idx++ to it) }
}