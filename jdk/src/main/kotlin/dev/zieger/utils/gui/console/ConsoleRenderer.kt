package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.gui2.InteractableRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import dev.zieger.utils.misc.groupByIndexed

open class ConsoleRenderer(
    private val refresh: suspend () -> Unit,
    private val remove: TextWithColor.() -> Unit
) : InteractableRenderer<ConsoleComponent> {

    override fun getPreferredSize(component: ConsoleComponent): TerminalSize = component.size
    override fun getCursorLocation(component: ConsoleComponent): TerminalPosition? = null

    override fun drawComponent(graphics: TextGUIGraphics, component: ConsoleComponent) {
        component.numOutputLines = graphics.putMsg(
            0, 0, component.scrollIdx, component.definition.commandPrefix, refresh, remove,
            *component.messages.toTypedArray()
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
                t.forEachIndexed { idx, c -> setCharacter(column + prefixLength + idx, row + maxRow + scrollIdx, c) }
                maxRow++
            }
        }
    return maxRow
}

fun List<TextWithColor>.lines(
    maxColumns: Int,
    refresh: suspend () -> Unit,
    remove: TextWithColor.() -> Unit
): List<List<TextCharacter>> {
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
                setCharacter(column + c, row + r, char)
            }
            maxRow++
        }
    return maxRow
}

private fun TextWithColor.characters(
    refresh: suspend () -> Unit,
    remove: TextWithColor.() -> Unit
): List<TextCharacter> {
    val text = text(TextScope(refresh) { remove() }).toString()
    return text.mapIndexed { idx, c ->
        val colorScope = ColorScope(text, c, idx)
        TextCharacter(c, color?.invoke(colorScope), background?.invoke(colorScope))
    }
}

private fun List<TextCharacter>.processBackSpaces(): List<TextCharacter> =
    filterIndexed { index, character ->
        character.character != '\b' && getOrNull(index + 1)?.character != '\b'
    }

private fun List<TextCharacter>.processTabs(): List<TextCharacter> =
    flatMap { textCharacter ->
        if (textCharacter.character == '\t')
            (0..3).map { TextCharacter(' ', textCharacter.foregroundColor, textCharacter.backgroundColor) }
        else listOf(textCharacter)
    }

private fun List<TextCharacter>.processNewLines(): List<List<TextCharacter>> {
    var line = 0
    return groupBy { if (it.character == '\n') ++line else line }
        .map { (_, tc) -> tc.filterNot { it.character == '\n' } }
}

private fun List<List<TextCharacter>>.wrapLines(columns: Int): List<List<TextCharacter>> =
    flatMap { it.groupByIndexed { idx, _ -> idx / columns.coerceAtLeast(1) }.map { (_, text) -> text } }
