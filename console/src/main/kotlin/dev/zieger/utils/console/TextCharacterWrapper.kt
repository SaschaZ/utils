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

    val textCharacter: TextCharacter
        get() = TextCharacter(character, foreground, background, *modifiers.toTypedArray())
}

typealias TextChar = () -> TextCharacterWrapper
typealias TextString = () -> Array<TextChar>