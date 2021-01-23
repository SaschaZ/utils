@file:Suppress("unused")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbstractInteractableComponent
import com.googlecode.lanterna.gui2.InteractableRenderer
import com.googlecode.lanterna.gui2.Window
import dev.zieger.utils.UtilsSettings
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.misc.*
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlin.collections.lastOrNull

open class ConsoleComponent(
    private val screen: PanelScreen,
    scope: CoroutineScope,
    private val window: Window,
    internal val definition: ConsoleDefinition,
    minRefreshInterval: IDurationEx = 100.milliseconds,
    sizeCheckInterval: IDurationEx = 100.milliseconds
) : AbstractInteractableComponent<ConsoleComponent>(), CoroutineScope by scope {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            UtilsSettings.PRINT_EXCEPTIONS = true
            console(
                ConsoleDefinition(0.0.rel to 0.0.rel, 0.5.rel to 0.5.rel),
                ConsoleDefinition(0.5.rel to 0.0.rel, 1.0.rel to 0.5.rel),
                ConsoleDefinition(0.0.rel to 0.5.rel, 1.0.rel to 1.0.rel),
                title = "TestConsole"
            ) {
                var cnt = 0
                outnl(0, text { "just a test ${cnt++}" })
                outnl(1, text { "just a test #${cnt++}" })
                outnl(2, +"just a test #2")
            }
        }.asUnit()
    }

    private val buffer = ArrayList<Message>()
    val messages: List<Message> get() = ArrayList(buffer)
    private val bufferMutex = Mutex()

    private val antiSpamProxy = AntiSpamProxy(minRefreshInterval, scope)

    private var lastSize: TerminalSize? = null

    init {
        position = definition.position(screen.terminalSize)
        size = definition.size(screen.terminalSize)

        scope.launchEx(interval = sizeCheckInterval) { checkSize() }
    }

    override fun isFocusable(): Boolean = !definition.hasCommandInput

    internal var scrollIdx by OnChanged(0) { refresh() }
        private set
    internal var numOutputLines: Int? by OnChanged(null) { value ->
        whenNotNull(value, previousValue) { v, pv ->
            val bottom = size.rows - scrollIdx
//            Log.v("v=$v; pv=$pv; r=${screen.size.rows}; scrollIdx=$scrollIdx; bottom=$bottom")
            if (bottom in min(pv, v)..max(pv, v))
                scroll(-v + bottom)
        }
    }

    internal fun scroll(delta: Int) {
        scrollIdx = when {
            delta > 0 -> (scrollIdx + delta).coerceAtMost(0)
            delta < 0 -> (scrollIdx + delta).coerceAtLeast(-(numOutputLines ?: 1) + size.rows)
            else -> scrollIdx
        }// logV { "delta=$delta; scrollIdx=$it" }
    }

    fun newMessage(message: Message): () -> Unit {
        launchEx(mutex = bufferMutex) {
            when {
                message.offset != null ->
                    buffer.add((buffer.size - message.offset).coerceIn(0..buffer.size), message)
                buffer.lastOrNull()?.newLine == false ->
                    buffer[buffer.lastIndex] = (buffer.last().merge(message))
                else -> buffer.add(message)
            }
            refresh()
        }

        return { remove(message) }
    }

    private operator fun Message.plus(other: Message): Message = merge(other)

    fun refresh() = antiSpamProxy { invalidate() }

    override fun createDefaultRenderer(): InteractableRenderer<ConsoleComponent> =
        ConsoleRenderer({ refresh() }) { remove(this) }

    private fun checkSize() {
        val prevSize = lastSize
        lastSize = screen.terminalSize?.also { newSize ->
            if (newSize != prevSize) {
                position = definition.position(newSize)
                size = definition.size(newSize)
//                Log.v("position=$position; size=$size")
                refresh()
            }
        } ?: prevSize
    }

    fun remove(message: Message) = launchEx(mutex = bufferMutex) {
        buffer.removeAll { it.hasId(message.id) }
        refresh()
    }.asUnit()

    fun remove(text: TextWithColor) = launchEx(mutex = bufferMutex) {
        buffer.removeAll { it.texts.any { m -> m.id == text.id } }
        refresh()
    }.asUnit()
}