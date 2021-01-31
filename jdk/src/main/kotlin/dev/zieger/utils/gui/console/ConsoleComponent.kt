@file:Suppress("unused")

package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
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
    screen: PanelScreen,
    scope: CoroutineScope,
    private val window: Window,
    definition: ConsoleDefinition,
    minRefreshInterval: IDurationEx = 100.milliseconds
) : AbsConsoleComponent<ConsoleComponent>(definition, screen, scope, !definition.hasCommandInput),
    CoroutineScope by scope {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            UtilsSettings.PRINT_EXCEPTIONS = true
            console(
                ConsoleDefinition(0.0.rel to 0.0.rel, 0.5.rel to 0.5.rel),
                ConsoleDefinition(0.5.rel to 0.0.rel, 1.0.rel to 0.5.rel),
                ConsoleDefinition(0.0.rel to 0.5.rel, 1.0.rel to 1.0.rel, false) { scr, s, _, d ->
                    SystemInfoComponent(d, scr, s)
                },
                title = "TestConsole"
            ) {
                var cnt = 0
                outnl(0, text { "just a test ${cnt++}" })
                outnl(1, text { "just a test #${cnt++}" })
//                outnl(2, +"just a test #2")
            }
        }.asUnit()
    }

    private val buffer = ArrayList<Message>()
    private val bufferMutex = Mutex()

    private val antiSpamProxy = AntiSpamProxy(minRefreshInterval, scope)

    private var scrollIdx by OnChanged(0) { refresh() }
    private var numOutputLines: Int? by OnChanged(null) { value ->
        whenNotNull(value, previousValue) { v, pv ->
            val bottom = size.rows - scrollIdx
//            Log.v("v=$v; pv=$pv; r=${screen.size.rows}; scrollIdx=$scrollIdx; bottom=$bottom")
            if (bottom in min(pv, v)..max(pv, v))
                scroll(-v + bottom)
        }
    }

    internal fun scroll(delta: Int) {
        scrollIdx = when {
            (numOutputLines ?: 1) < size.rows -> 0
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

    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result = keyStroke.run {
//        Log.v("keyStroke=$keyStroke")
        when {
            !isAltDown && !isCtrlDown -> {
                when (keyType) {
                    KeyType.ArrowUp -> {
                        scroll(5)
                        Interactable.Result.HANDLED
                    }
                    KeyType.ArrowDown -> {
                        scroll(-5)
                        Interactable.Result.HANDLED
                    }
                    else -> Interactable.Result.UNHANDLED
                }
            }
            isAltDown -> when (keyType) {
                KeyType.ArrowLeft -> Interactable.Result.MOVE_FOCUS_PREVIOUS
                KeyType.ArrowRight -> Interactable.Result.MOVE_FOCUS_NEXT
                KeyType.ArrowUp -> Interactable.Result.MOVE_FOCUS_UP
                KeyType.ArrowDown -> Interactable.Result.MOVE_FOCUS_DOWN
                else -> Interactable.Result.UNHANDLED
            }
            else -> Interactable.Result.UNHANDLED
        }
    }

    fun refresh() = antiSpamProxy { invalidate() }

    fun remove(message: Message) = launchEx(mutex = bufferMutex) {
        buffer.removeAll { it.hasId(message.id) }
        refresh()
    }.asUnit()

    fun remove(text: TextWithColor) = launchEx(mutex = bufferMutex) {
        buffer.removeAll { it.texts.any { m -> m.id == text.id } }
        refresh()
    }.asUnit()

    override fun drawComponent(graphics: TextGUIGraphics, component: ConsoleComponent) {
        numOutputLines = graphics.putMsg(
            0, 0, scrollIdx, definition.logMessagePrefix, { refresh() }, { remove(this) },
            *buffer.toTypedArray()
        )
    }
}

fun TextGUIGraphics.putMsg(
    column: Int, row: Int, scrollIdx: Int,
    prefix: TextWithColor = text(""),
    refresh: suspend () -> Unit,
    remove: TextWithColor.() -> Unit,
    vararg message: Message
): Int {
    val prefixLength = prefix.text(TextScope({}, {})).toString().length
    var maxRow = 0
    message.map { it.texts.lines(size.columns - prefixLength, refresh, remove) }
        .forEach {
            putText(column, row + maxRow + scrollIdx, refresh, remove, prefix)
            it.forEach { t ->
                t.forEachIndexed { idx, c ->
                    setCharacter(
                        column + prefixLength + idx, row + maxRow + scrollIdx,
                        c.textCharacter
                    )
                }
                maxRow++
            }
        }
    return maxRow
}

fun List<TextWithColor>.lines(
    maxColumns: Int,
    refresh: suspend () -> Unit,
    remove: TextWithColor.() -> Unit
): List<List<TextCharacterWrapper>> {
    return flatMap { it.characters(refresh, remove) }
        .processBackSpaces()
        .processTabs()
        .processNewLines()
        .wrapLines(maxColumns)
}

fun TextGUIGraphics.putText(
    column: Int, row: Int,
    refresh: suspend () -> Unit,
    remove: TextWithColor.() -> Unit,
    vararg text: TextWithColor
): Int {
    var maxRow = 0
    text.toList()
        .lines(size.columns - column, refresh, remove)
        .forEachIndexed { r, chars ->
            chars.forEachIndexed { c, char ->
                setCharacter(column + c, row + r, char.textCharacter)
            }
            maxRow++
        }
    return maxRow
}

private fun TextWithColor.characters(
    refresh: suspend () -> Unit,
    remove: TextWithColor.() -> Unit
): List<TextCharacterWrapper> {
    val textScope = TextScope(refresh) { remove() }
    val text = text(textScope).toString()
    return text.mapIndexed { idx, c ->
        val colorScope = ColorScope(text, c, idx, textScope)
        TextCharacterWrapper(c, color?.invoke(colorScope), background?.invoke(colorScope))
    }
}

private fun List<TextCharacterWrapper>.processBackSpaces(): List<TextCharacterWrapper> =
    filterIndexed { index, character ->
        character.character != '\b' && getOrNull(index + 1)?.character != '\b'
    }

private fun List<TextCharacterWrapper>.processTabs(): List<TextCharacterWrapper> =
    flatMap { TextCharacterWrapper ->
        if (TextCharacterWrapper.character == '\t')
            (0..3).map {
                TextCharacterWrapper(
                    ' ',
                    TextCharacterWrapper.foregroundColor,
                    TextCharacterWrapper.backgroundColor
                )
            }
        else listOf(TextCharacterWrapper)
    }

private fun List<TextCharacterWrapper>.processNewLines(): List<List<TextCharacterWrapper>> {
    var line = 0
    return groupBy { if (it.character == '\n') ++line else line }
        .map { (_, tc) -> tc.filterNot { it.character == '\n' } }
}

private fun List<List<TextCharacterWrapper>>.wrapLines(columns: Int): List<List<TextCharacterWrapper>> =
    flatMap { it.groupByIndexed { idx, _ -> idx / columns.coerceAtLeast(1) }.map { (_, text) -> text } }

data class TextCharacterWrapper(
    val character: Char,
    val foregroundColor: TextColor?,
    val backgroundColor: TextColor?
) {
    val textCharacter get() = TextCharacter(character, foregroundColor, backgroundColor)
}