package dev.zieger.utils.console

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor

data class TextCharacterWrapper(
    val character: Char,
    val foreground: TextColor = TextColor.ANSI.DEFAULT,
    val background: TextColor = TextColor.ANSI.DEFAULT,
    val modifiers: List<SGR> = emptyList()
) {
    constructor(
        character: Char,
        foreground: TextColor = TextColor.ANSI.DEFAULT,
        background: TextColor = TextColor.ANSI.DEFAULT,
        vararg modifiers: SGR
    ) : this(character, foreground, background, modifiers.toList())

    internal val textCharacter: TextCharacter
        get() = TextCharacter(character, foreground, background, *modifiers.toTypedArray())
}

typealias TextString = Array<TextCharacterWrapper>

data class TextBuilderScope(val row: Int, val col: Int)
typealias TextBuilder = TextBuilderScope.() -> TextString

operator fun Any.unaryPlus(): TextString =
    toString().map { TextCharacterWrapper(it) }.toTypedArray()

operator fun TextString.times(color: TextColor): TextString =
    map { it.copy(foreground = color) }.toTypedArray()

operator fun TextString.div(color: TextColor): TextString =
    map { it.copy(background = color) }.toTypedArray()

operator fun TextString.rem(sgr: SGR): TextString =
    map { it.copy(modifiers = it.modifiers + sgr) }.toTypedArray()