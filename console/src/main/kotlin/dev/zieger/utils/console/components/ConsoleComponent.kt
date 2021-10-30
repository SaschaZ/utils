@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.console.components

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import dev.zieger.utils.console.ConsoleInstances.SIZE_SCOPE
import dev.zieger.utils.console.ConsoleInstances.UI_SCOPE
import dev.zieger.utils.console.ConsoleRenderer
import dev.zieger.utils.console.TextBuilder
import dev.zieger.utils.console.dto.ConsoleOptions
import dev.zieger.utils.koin.DI
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.koin.core.component.get
import java.util.*

open class ConsoleComponent : AbstractFocusableComponent<ConsoleComponent>(), FocusableConsoleComponent {

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
                    for (builder in bufferChannel) {
                        rawBuffer.add(builder)
                        onBufferChanged()
                        scrollToBottom()
                    }
                }
            }
        }
    private val rawBuffer = LinkedList<TextBuilder>()
    internal var renderBuffer = LinkedList<TextBuilder>()
        private set
    internal var renderedLines = 0
    private val bufferChannel = Channel<TextBuilder>(Channel.UNLIMITED)

    private var lastSize: TerminalSize? = null
    internal var scrollIdx = 0
        private set

    override fun createDefaultRenderer() = ConsoleRenderer()

    private fun onBufferChanged() {
        renderBuffer = LinkedList(rawBuffer)
        invalidate()
    }

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
            val bufferLines = renderedLines
            val lowerBound =
                (-bufferLines.coerceAtLeast(position.row) + size.rows - position.row).coerceAtMost(-position.row)
            val upperBound = -position.row
            scrollIdx = (scrollIdx + delta).coerceIn(lowerBound..upperBound)

            onBufferChanged()
        }
    }

    private suspend fun scrollToTop() = scroll(renderedLines)

    private suspend fun scrollToBottom() = scroll(-renderedLines)

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

    override fun out(builder: TextBuilder) =
        bufferChannel.offer(builder).asUnit()

    override fun release() {
        di?.release()
    }
}