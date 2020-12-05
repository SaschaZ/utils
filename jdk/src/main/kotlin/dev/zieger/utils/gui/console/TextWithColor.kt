package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.BLACK
import com.googlecode.lanterna.TextColor.ANSI.WHITE
import java.util.concurrent.atomic.AtomicLong

data class TextWithColor(
    val text: MessageBuilder,
    val color: MessageColor,
    val background: MessageColor,
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
        color: TextColor,
        background: TextColor,
        newLine: Boolean = false
    ) : this(text, { color }, { background }, newLine)

    val id: MessageId = newId
}

typealias MessageId = Long
typealias MessageBuilder = MessageScope.() -> Any
typealias MessageColor = MessageColorScope.(idx: Int) -> TextColor

data class MessageColorScope(
    val message: String,
    val character: Char
)

operator fun TextColor.invoke(bg: TextColor = BLACK, msg: MessageBuilder) = TextWithColor(msg, this, bg)
operator fun TextColor.invoke(msg: Any, bg: TextColor = BLACK) = TextWithColor({ msg }, this, bg)

operator fun TextWithColor.times(text: TextWithColor): List<TextWithColor> = listOf(this, text)
operator fun TextWithColor.times(text: String): List<TextWithColor> = listOf(this, WHITE(text))
operator fun TextWithColor.times(text: List<TextWithColor>): List<TextWithColor> =
    listOf(this, *text.toTypedArray())

operator fun String.times(text: TextWithColor): List<TextWithColor> = listOf(WHITE(this), text)
operator fun String.times(text: String): List<TextWithColor> = listOf(
    WHITE(this),
    WHITE(text)
)

operator fun String.times(text: List<TextWithColor>): List<TextWithColor> =
    listOf(WHITE(this), *text.toTypedArray())

operator fun List<TextWithColor>.times(text: TextWithColor): List<TextWithColor> =
    listOf(*toTypedArray(), text)

operator fun List<TextWithColor>.times(text: String): List<TextWithColor> =
    listOf(*toTypedArray(), WHITE(text))

operator fun List<TextWithColor>.times(text: List<TextWithColor>): List<TextWithColor> =
    listOf(*toTypedArray(), *text.toTypedArray())