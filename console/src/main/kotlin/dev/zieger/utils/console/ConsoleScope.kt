package dev.zieger.utils.console

import com.googlecode.lanterna.TextColor

interface ConsoleOwnerScope : ConsoleScope {

    var activeComponent: Int
}

interface ConsoleScope {

    fun out(str: TextString)
    fun out(str: Any) = out(+str)

    fun outNl(str: TextString)
    fun outNl(str: Any = "") = outNl(+str)

    fun textCharacter(character: Char): TextCharacterWrapper

    operator fun (() -> Any).unaryPlus(): TextString =
        { this().toString().map { { textCharacter(it) } }.toTypedArray() }

    operator fun Any.unaryPlus(): TextString =
        { toString().map { { textCharacter(it) } }.toTypedArray() }

    operator fun TextString.times(foreground: TextColor): TextString = {
        this().map { { it().copy(foreground = foreground) } }.toTypedArray()
    }

    operator fun Any.times(foreground: TextColor): TextString = +this * foreground

    operator fun TextString.times(foreground: () -> TextColor): TextString = {
        this().map { { it().copy(foreground = foreground()) } }.toTypedArray()
    }

    operator fun Any.times(foreground: () -> TextColor): TextString = +this * foreground

    operator fun TextString.div(background: TextColor): TextString = {
        this().map { { it().copy(background = background) } }.toTypedArray()
    }

    operator fun Any.div(background: TextColor): TextString {
        return +this / background
    }

    operator fun TextString.div(background: () -> TextColor): TextString = {
        this().map { { it().copy(background = background()) } }.toTypedArray()
    }

    operator fun Any.div(background: () -> TextColor): TextString {
        return +this / background
    }
}