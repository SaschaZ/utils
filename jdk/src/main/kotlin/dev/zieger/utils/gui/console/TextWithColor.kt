package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import java.util.concurrent.atomic.AtomicLong

@Suppress("DataClassPrivateConstructor")
data class TextWithColor internal constructor(
    val text: MessageBuilder,
    val color: MessageColor? = null,
    val background: MessageColor? = null,
    val newLine: Boolean = false,
    var visible: Boolean = true,
    var active: Boolean = true,
    val groupId: Long = NO_GROUP_ID
) {

    companion object {

        const val NO_GROUP_ID = -1L

        private val lastGroupId = AtomicLong(0L)
        val newGroupId get() = lastGroupId.getAndIncrement()

        private val lastId = AtomicLong(0)
        internal val newId: MessageId get() = lastId.getAndIncrement()
    }

    constructor(
        text: MessageBuilder,
        color: TextColor? = null,
        background: TextColor? = null,
        newLine: Boolean = false
    ) : this(text, { color }, { background }, newLine)

    val id: MessageId = newId
}

typealias text = TextWithColor

typealias MessageId = Long
typealias MessageBuilder = MessageScope.() -> Any
typealias MessageColor = MessageColorScope.() -> TextColor?


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

data class MessageColorScope(
    val message: String,
    val character: Char,
    val idx: Int
)


fun text(builder: MessageBuilder): TextWithColor = TextWithColor(builder)
operator fun String.unaryPlus(): TextWithColor = text { this@unaryPlus }

operator fun TextWithColor.times(color: MessageColor): TextWithColor = copy(color = color)
operator fun TextWithColor.times(color: TextColor): TextWithColor = copy(color = { color })
operator fun MessageBuilder.times(color: MessageColor): TextWithColor = text(this@times, color)
operator fun MessageBuilder.times(color: TextColor): TextWithColor = text(this@times, { color })
operator fun String.times(color: MessageColor): TextWithColor = text({ this@times }, color)
operator fun String.times(color: TextColor): TextWithColor = text({ this@times }, { color })

operator fun TextWithColor.div(background: MessageColor): TextWithColor = copy(background = background)
operator fun TextWithColor.div(background: TextColor): TextWithColor = copy(background = { background })
operator fun MessageBuilder.div(background: MessageColor): TextWithColor = text(this@div, background = background)
operator fun MessageBuilder.div(background: TextColor): TextWithColor = text(this@div, background = { background })
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