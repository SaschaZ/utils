package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import java.util.concurrent.atomic.AtomicLong

data class TextWithColor(
    val text: MessageText,
    val color: MessageColor? = null,
    val background: MessageColor? = null
) {

    companion object {

        private val lastId = AtomicLong(0L)
        private val newId: TextId get() = lastId.getAndIncrement()
    }

    constructor(
        text: Any,
        color: TextColor? = null,
        background: TextColor? = null
    ) : this({ text }, { color }, { background })

    val id: TextId = newId
}

typealias text = TextWithColor

typealias TextId = Long
typealias MessageId = Long
typealias MessageText = ITextScope.() -> Any
typealias MessageColor = IColorScope.() -> TextColor?

class Message internal constructor(
    initial: List<TextWithColor>,
    val newLine: Boolean = false,
    val offset: Int? = null,
    ids: List<MessageId> = emptyList()
) {
    companion object {

        private val lastId = AtomicLong(0L)
        private val newId: MessageId get() = lastId.getAndIncrement()
    }

    constructor(
        text: TextWithColor,
        newLine: Boolean = false,
        offset: Int? = null
    ) : this(listOf(text), newLine, offset)

    val id: MessageId = newId
    private val ids: List<MessageId> = ids + id

    private val internalTexts = ArrayList(initial)
    val texts: List<TextWithColor> get() = internalTexts

    fun hasId(id: MessageId) = ids.contains(id)

    fun merge(other: Message): Message =
        Message(
            texts + other.texts, newLine || other.newLine,
            offset ?: other.offset, listOf(id, other.id)
        )

    fun remove(id: TextId) = internalTexts.removeAll { it.id == id }
}

interface ITextScope {
    val refresh: suspend () -> Unit
    val remove: () -> Unit
}

data class TextScope(
    override val refresh: suspend () -> Unit,
    override val remove: () -> Unit
) : ITextScope

interface IColorScope : ITextScope {
    val message: String
    val character: Char
    val idx: Int
}

class ColorScope(
    override val message: String,
    override val character: Char,
    override val idx: Int,
    textScope: ITextScope
) : IColorScope, ITextScope by textScope


fun text(text: MessageText): TextWithColor = TextWithColor(text)
operator fun String.unaryPlus(): TextWithColor = text { this@unaryPlus }

operator fun TextWithColor.times(color: MessageColor): TextWithColor = copy(color = color)
operator fun TextWithColor.times(color: TextColor): TextWithColor = copy(color = { color })
operator fun MessageText.times(color: MessageColor): TextWithColor = text(this@times, color)
operator fun MessageText.times(color: TextColor): TextWithColor = text(this@times, { color })
operator fun String.times(color: MessageColor): TextWithColor = text({ this@times }, color)
operator fun String.times(color: TextColor): TextWithColor = text({ this@times }, { color })

operator fun TextWithColor.div(background: MessageColor): TextWithColor = copy(background = background)
operator fun TextWithColor.div(background: TextColor): TextWithColor = copy(background = { background })
operator fun MessageText.div(background: MessageColor): TextWithColor = text(this@div, background = background)
operator fun MessageText.div(background: TextColor): TextWithColor = text(this@div, background = { background })
operator fun String.div(background: MessageColor): TextWithColor = text({ this@div }, background = background)
operator fun String.div(background: TextColor): TextWithColor = text({ this@div }, background = { background })

operator fun TextWithColor.plus(text: TextWithColor): List<TextWithColor> = listOf(this, text)
operator fun TextWithColor.plus(text: String): List<TextWithColor> = listOf(this, text { text })
operator fun TextWithColor.plus(text: List<TextWithColor>): List<TextWithColor> = listOf(this, *text.toTypedArray())

operator fun List<TextWithColor>.plus(text: TextWithColor): List<TextWithColor> =
    listOf(*toTypedArray(), text)

operator fun List<TextWithColor>.plus(text: String): List<TextWithColor> =
    listOf(*toTypedArray(), +text)

operator fun List<TextWithColor>.plus(text: List<TextWithColor>): List<TextWithColor> =
    listOf(*toTypedArray(), *text.toTypedArray())