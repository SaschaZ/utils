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
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.base.times
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class MessageBuffer(
    private val scope: CoroutineScope,
    private val columns: () -> Int,
    private val size: Int = BUFFER_SIZE,
    spamDuration: IDurationEx = SPAM_DURATION,
    private val onUpdate: (ScreenBuffer) -> Unit
) {

    companion object {

        private const val BUFFER_SIZE = 4098
        internal val SPAM_DURATION = 500.milliseconds

        internal data class Entity(val text: MutableList<TextWithColor>) {

            companion object {
                private val lastId = AtomicLong(0)
                val newId get() = lastId.getAndIncrement()
            }

            val id: Long = newId
        }
    }

    private val buffer = ArrayList<Entity>(size)
    private val bufferMutex = Mutex()
    private val messageChannel = Channel<Message>(Channel.UNLIMITED)
    private val antiSpamProxy = AntiSpamProxy(spamDuration * 0.95, scope)

    init {
        scope.launchEx {
            messageChannel.forEach { (text, autoRefresh, newLine, offset) ->
                bufferMutex.withLock {
                    var doUpdate = false

                    val value = (buffer.lastOrNull()
                        ?.nullWhen { (text) -> offset != null || newLine || text.last().newLine }
                        ?.let { doUpdate = true; it.copy(text = (it.text + text.toList()).toMutableList()) }
                        ?: Entity(text.toMutableList())).let {
                        it.copy(text = it.text.mapIndexed { idx, t ->
                            if (newLine && idx == text.lastIndex) t.copy(newLine = true) else t
                        }.toMutableList())
                    }

                    when {
                        offset != null -> buffer.add(buffer.size - offset, value)
                        doUpdate -> buffer[buffer.lastIndex] = value
                        else -> buffer.add(value)
                    }

                    while (buffer.size >= size) buffer.removeAt(0)
                    if (autoRefresh) this@MessageBuffer.scope.launchEx { update() }
                }
            }
        }
    }

    var lastScreenBuffer: ScreenBuffer? = null
        private set

    suspend fun update() = antiSpamProxy { onUpdate(screenBuffer()) }

    fun addMessage(msg: Message): () -> Unit =
        messageChannel.offer(msg).let { { msg.message.runEach { remove() } } }

    private fun TextWithColor.remove(): Unit = scope.launchEx {
        bufferMutex.withLock {
            buffer.forEach { e -> e.text.removeAll { it.id == id } }
            buffer.removeAll { e -> e.text.isEmpty() }
        }
        update()
    }.asUnit()

    private fun Entity.remove(): Unit = scope.launchEx {
        bufferMutex.withLock {
            buffer.removeAll { e -> id == e.id }
        }
        update()
    }.asUnit()

    suspend fun screenBuffer(): ScreenBuffer = bufferMutex.withLock {
        fun Entity.line() = text.map {
            val scope = MessageScopeImpl({ update() }, { remove() })
            it to it.text(scope).toString()
        }.flatMap { (text, str) ->
            str.mapIndexed { idx, character ->
                MessageColorScope(str, character, idx).let { s ->
                    ScreenChar(character, text.color?.invoke(s), text.background?.invoke(s))
                }
            }
        }.line

        fun ScreenLine.group(columns: Int): ScreenLineGroup {
            var idx = 0
            var nlCnt = 0

            return replace('\t') { _, tabIdx -> (0..tabIdx % 4).joinToString("") { " " } }
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
        }

        buffer.map { it.line().group(columns()) }.buffer.also { lastScreenBuffer = it }
    }

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