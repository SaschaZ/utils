@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.BLACK
import com.googlecode.lanterna.TextColor.ANSI.YELLOW
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.forEach
import dev.zieger.utils.misc.AntiSpamProxy
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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
    private val messageChannel = Channel<Message>(Channel.UNLIMITED)
    private val antiSpamProxy = AntiSpamProxy(spamDuration, scope)
    private val lastMessageBuffer = HashMap<MessageId, String>()

    init {
        scope.launchEx {
            messageChannel.forEach { (text, autoRefresh, newLine, offset) ->
                var doUpdate = false
                val value = (buffer.lastOrNull()
                    ?.nullWhen { n -> offset > 0 || newLine || n.last().newLine }
                    ?.let { l -> doUpdate = true; l + text.toList() } ?: text.toList())
                    .mapIndexed { idx, t -> if (newLine && idx == text.lastIndex) t.copy(newLine = true) else t }

                when {
                    offset > 0 -> buffer.add(buffer.size - offset, value)
                    doUpdate -> buffer[buffer.lastIndex] = value
                    else -> buffer.add(value)
                }

                while (buffer.size >= size) buffer.removeAt(0)
                if (autoRefresh) update()
            }
        }
    }

    fun update() = antiSpamProxy { onUpdate(buildScreenBuffer()) }

    fun addMessage(msg: Message) = messageChannel.offer(msg).asUnit()

    private fun buildScreenBuffer(): ScreenBuffer = buffer.map { line ->
        var idx = 0
        var nlCnt = 0
        line.map {
            val scope = MessageScope({ update() }, { it.visible = false }, { it.active = false }, { it.active = true })
            val wasActive = it.active
            val message = it.text(scope).toString()
            it to when {
                it.visible -> ""
                it.active || wasActive -> message.also { s -> lastMessageBuffer[it.id] = s }
                else -> lastMessageBuffer[it.id] ?: ""
            }
        }.flatMap { (col, c) -> c.map { m -> m to col.color() to col.background() } }
            .replace('\t') { _, tabIdx -> (0..tabIdx % 4).joinToString("") { " " } }
            .remove('\b', 1)
            .groupBy { (c, _, _) ->
                if (c == '\n') nlCnt++
                columns().let { if (it > 3) nlCnt + idx++ / (it - 3) else 0 }
            }.values.toList()
            .mapIndexed { i, it -> (if (i == 0) ">: " else "   ").map { c -> c to YELLOW to BLACK } + it }
            .map { it.filterNot { it1 -> it1.first.isISOControl() } }
    }

    private fun ScreenLine.remove(
        from: Char,
        pre: Int = 0
    ): ScreenLine {
        val buffer = LinkedList<ScreenChar>()
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

    private fun ScreenLine.replace(
        from: Char,
        to: (Char, Int) -> String
    ): ScreenLine {
        var idx = 0
        return flatMap { value ->
            when (value.first) {
                from -> to(value.first, idx++).map { c -> c to value.second to value.third }
                else -> listOf(value)
            }
        }
    }
}

typealias ScreenChar = Triple<Char, TextColor, TextColor>
typealias ScreenLine = List<ScreenChar>
typealias ScreenLineGroup = List<ScreenLine>
typealias ScreenBuffer = List<ScreenLineGroup>

data class Message(
    val message: List<TextWithColor>,
    val autoRefresh: Boolean,
    val newLine: Boolean,
    val offset: Int
)

data class MessageScope(
    val refresh: () -> Unit,
    val remove: () -> Unit,
    val pause: () -> Unit,
    val resume: () -> Unit
)