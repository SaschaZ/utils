package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.BLACK
import com.googlecode.lanterna.TextColor.ANSI.WHITE
import java.util.concurrent.atomic.AtomicLong

data class TextWithColor(
    val text: MessageBuilder,
    val color: MessageColor? = null,
    val background: MessageColor? = null,
    val newLine: Boolean = false,
    var visible: Boolean = true,
    var active: Boolean = true
) {

    companion object {

        private val lastId = AtomicLong(0)

        val newId: MessageId get() = lastId.getAndIncrement()
    }

    constructor(
        text: MessageBuilder,
        color: TextColor? = null,
        background: TextColor? = null,
        newLine: Boolean = false
    ) : this(text, { color }, { background }, newLine)

    val id: MessageId = newId
}

typealias TWC = TextWithColor

typealias MessageId = Long
typealias MessageBuilder = MessageScope.() -> Any
typealias MessageColor = MessageColorScope.(idx: Int) -> TextColor?

data class MessageColorScope(
    val message: String,
    val character: Char
)

operator fun MessageBuilder.times(color: MessageColor): TextWithColor = TWC(this, color)
operator fun MessageBuilder.times(color: TextColor): TextWithColor = TWC(this, { color })
operator fun String.times(color: MessageColor): TextWithColor = TWC({ this }, color)
operator fun String.times(color: TextColor): TextWithColor = TWC({ this }, { color })

operator fun TextWithColor.div(background: MessageColor): TextWithColor = copy(background = background)
operator fun TextWithColor.div(background: TextColor): TextWithColor = copy(background = { background })
operator fun MessageBuilder.div(background: MessageColor): TextWithColor = TWC(this, background = background)
operator fun MessageBuilder.div(background: TextColor): TextWithColor = TWC(this, background = { background })
operator fun String.div(background: MessageColor): TextWithColor = TWC({ this }, background = background)
operator fun String.div(background: TextColor): TextWithColor = TWC({ this }, background = { background })

operator fun String.unaryPlus(): TextWithColor = TWC({ this })

operator fun TextWithColor.plus(text: TextWithColor): List<TextWithColor> = listOf(this, text)
operator fun TextWithColor.plus(text: String): List<TextWithColor> = listOf(this, TWC({ text }))
operator fun TextWithColor.plus(text: List<TextWithColor>): List<TextWithColor> =
    listOf(this, *text.toTypedArray())

operator fun List<TextWithColor>.plus(text: TextWithColor): List<TextWithColor> =
    listOf(*toTypedArray(), text)

operator fun List<TextWithColor>.plus(text: String): List<TextWithColor> =
    listOf(*toTypedArray(), +text)

operator fun List<TextWithColor>.plus(text: List<TextWithColor>): List<TextWithColor> =
    listOf(*toTypedArray(), *text.toTypedArray())