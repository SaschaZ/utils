@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.YELLOW
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.forEach
import dev.zieger.utils.gui.console.ScreenBuffer.Companion.buffer
import dev.zieger.utils.gui.console.ScreenLine.Companion.line
import dev.zieger.utils.gui.console.ScreenLineGroup.Companion.group
import dev.zieger.utils.misc.AntiSpamProxy
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class MessageBuffer(
    scope: CoroutineScope,
    private val columns: () -> Int,
    private val size: Int = BUFFER_SIZE,
    spamDuration: IDurationEx = SPAM_DURATION,
    private val onUpdate: (ScreenBuffer) -> Unit
) {

    companion object {

        private const val BUFFER_SIZE = 4098
        private val SPAM_DURATION = 100.milliseconds
    }

    private var buffer = ArrayList<List<TextWithColor>>(size)
    private val bufferMutex = Mutex()
    private val messageChannel = Channel<Message>(Channel.UNLIMITED)
    private val antiSpamProxy = AntiSpamProxy(spamDuration, scope)
    private val lastMessageBuffer = HashMap<MessageId, String>()

    init {
        scope.launchEx {
            messageChannel.forEach { (text, autoRefresh, newLine, offset) ->
                bufferMutex.withLock {
                    var doUpdate = false
                    val value = (buffer.lastOrNull()
                        ?.nullWhen { n -> offset > 0 || newLine || n.last().newLine }
                        ?.let { l -> doUpdate = true; l + text.toList() } ?: text.toList())
                        .mapIndexed { idx, t -> if (newLine && idx == text.lastIndex) t.copy(newLine = true) else t }

                    when {
                        offset > 0 -> buffer.add(buffer.lastIndex - offset, value)
                        doUpdate -> buffer[buffer.lastIndex] = value
                        else -> buffer.add(value)
                    }

                    while (buffer.size >= size) buffer.removeAt(0)
                    if (autoRefresh) update()
                }
            }
        }
    }

    var lastScreenBuffer: ScreenBuffer? = null
        private set

    suspend fun update() = onUpdate(bufferMutex.withLock { buildScreenBuffer().also { lastScreenBuffer = it } })

    fun addMessage(msg: Message) = messageChannel.offer(msg).let { { msg.message.forEach { it.visible = false } } }

    private fun buildScreenBuffer(): ScreenBuffer = buffer.map { line ->
        var idx = 0
        var nlCnt = 0
        val columns = columns()
        line.map {
            val scope =
                MessageScope({ update() }, { it.visible = false }, { it.active = false }, { it.active = true })
            val wasActive = it.active
            val message = it.text(scope).toString()
            it to when {
                !it.visible -> ""
                it.active || wasActive -> message.also { s -> lastMessageBuffer[it.id] = s }
                else -> lastMessageBuffer[it.id] ?: ""
            }
        }.filter { it.first.visible }
            .flatMap { (col, c) ->
                c.mapIndexed { idx, m ->
                    MessageColorScope(c, m).run {
                        ScreenChar(m, col.color?.invoke(this, idx), col.background?.invoke(this, idx))
                    }
                }
            }.line
            .replace('\t') { _, tabIdx -> (0..tabIdx % 4).joinToString("") { " " } }
            .remove('\b', 1)
            .groupBy { sc ->
                if (sc?.character == '\n') {
                    nlCnt++
                    idx = 0
                }
                if (columns > 3) nlCnt + idx++ / (columns - 3) else 0
            }.values.toList()
            .mapIndexed { i, it -> (if (i == 0) ">: " else "   ").map { c -> ScreenChar(c, YELLOW) } + it }
            .map { it.filterNot { it1 -> it1?.character?.isISOControl() == true }.line }.group
    }.buffer

    private fun ScreenLine.remove(
        from: Char,
        pre: Int = 0
    ): ScreenLine {
        val buffer = LinkedList<ScreenChar?>()
        return (flatMap {
            buffer.add(it)
            when {
                it?.character == from -> {
                    buffer.clear()
                    emptyList()
                }
                buffer.size == pre + 1 -> listOf(buffer.removeAt(0))
                else -> emptyList()
            }
        } + buffer).line
    }

    private fun ScreenLine.replace(
        from: Char,
        to: (Char, Int) -> String
    ): ScreenLine {
        var idx = 0
        return flatMap { value ->
            when (value?.character) {
                from -> to(value.character, idx++).map { c -> value.copy(character = c) }
                else -> listOf(value)
            }
        }.line
    }
}

data class ScreenChar(
    val character: Char,
    val foreground: TextColor? = null,
    val background: TextColor? = null
)

class ScreenLine(line: List<ScreenChar?>) : List<ScreenChar?> by line {
    companion object {
        val List<ScreenChar?>.line: ScreenLine get() = ScreenLine(this)
    }
}

class ScreenLineGroup(group: List<ScreenLine>) : List<ScreenLine> by group {
    companion object {
        val List<ScreenLine>.group: ScreenLineGroup get() = ScreenLineGroup(this)
    }
}

class ScreenBuffer(buffer: List<ScreenLineGroup>) : List<ScreenLineGroup> by buffer {
    companion object {
        val List<ScreenLineGroup>.buffer: ScreenBuffer get() = ScreenBuffer(this)
    }
}

data class Message(
    val message: List<TextWithColor>,
    val autoRefresh: Boolean,
    val newLine: Boolean,
    val offset: Int
)

data class MessageScope(
    val refresh: suspend () -> Unit,
    val remove: () -> Unit,
    val pause: () -> Unit,
    val resume: () -> Unit
)