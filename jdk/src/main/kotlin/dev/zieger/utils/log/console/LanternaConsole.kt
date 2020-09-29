@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.log.console

import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.minutes
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

data class MessageScope(val refresh: () -> Unit)
typealias Message = MessageScope.() -> Any
typealias MessageStore = Triple<Boolean, Boolean, Message>

class LanternaConsole(bufferSize: Int = BUFFER_SIZE) {

    companion object {

        private const val BUFFER_SIZE = 1024

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            LanternaConsole().scope {
                outNl("FooBooFo\noBooFooBooFoFoFooBFooFFoFooBoooBooFooBooooBooBoooFFooBooooBooFooBoooFoFooFooBooBooFFooBooooBoooBoooBoooBooFooBooFooBoo")
                out("mooobooo\n")
                out("määäää")

                var printed = 0
                var job: Job? = null
                out {
                    if (printed == 0) {
                        job = job ?: launchEx(interval = 2.seconds, delayed = 2.seconds) { printed++; refresh() }
                        " boofoo$printed "
                    } else "  DU HURENSOHN$printed  "
                }
                repeat(200) { out("$it|", false) }
                refresh()
                delay(1.minutes)
            }
        }.asUnit()
    }

    private val screen: Screen = DefaultTerminalFactory().createScreen()

    private val buffer = FiFo<MessageStore>(bufferSize)

    private fun onBufferChanged() {
        screen.clear()
        screen.newTextGraphics().run {
            var nlIdx = 1
            buffer.reversed().forEachIndexed { index, (_, _, message) ->
                message(MessageScope { onBufferChanged() }).toString().split("\n")
                    .flatMap { fm ->
                        if (fm.isEmpty()) listOf(" ") else fm.chunkedSequence(size.columns - 3).toList()
                    }.mapIndexed { idx, s -> if (idx == 0) ">: $s" else "   $s" }.reversed()
                    .forEach { putString(0, size.rows - nlIdx++, it) }
                if (index - nlIdx == size.rows) return@forEachIndexed
            }
        }
        screen.refresh()
    }

    val scope: Scope
        get() {
            screen.startScreen()
            return Scope()
        }

    fun release() = screen.stopScreen()

    inner class Scope {

        fun out(autoRefresh: Boolean = true, msg: Message) {
            val last = buffer.lastOrNull().nullWhen { it.second }
            buffer.put(last.concat(autoRefresh, false, msg), last != null)
            if (autoRefresh) onBufferChanged()
        }

        fun out(msg: Any, autoRefresh: Boolean = true) = out(autoRefresh) { msg }

        fun outNl(autoRefresh: Boolean = true, msg: Message) {
            buffer.put(autoRefresh to true to msg)
            if (autoRefresh) onBufferChanged()
        }

        fun outNl(msg: Any, autoRefresh: Boolean = true) = outNl(autoRefresh) { msg }

        private fun MessageStore?.concat(
            aR: Boolean,
            wNl: Boolean,
            msg: Message
        ): MessageStore = this?.let { (autoRefresh, withNl, message) ->
            val newMsg: Message = { "${message(this)}${msg()}" }
            (autoRefresh || aR) to (withNl || wNl) to newMsg
        } ?: (aR to wNl to msg)

        fun refresh() = onBufferChanged()

        fun release() = this@LanternaConsole.release()
    }
}

inline fun LanternaConsole.scope(block: LanternaConsole.Scope.() -> Unit) = scope.block()

infix fun <A, B, C> Pair<A, B>.to(other: C): Triple<A, B, C> = Triple(first, second, other)