package dev.zieger.utils.console

import com.googlecode.lanterna.TextColor

interface ConsoleOwnerScope : ConsoleScope {

    var focusedComponent: Int

    fun out(componentId: Int, str: TextString)
    fun out(componentId: Int, str: Any) = out(componentId, +str)

    fun outNl(componentId: Int, str: TextString)
    fun outNl(componentId: Int, str: Any = "") = outNl(componentId, +str)

    override fun out(str: TextString) = out(focusedComponent, str)
    override fun outNl(str: TextString) = outNl(focusedComponent, str)
}

interface ConsoleScope {

    fun out(str: TextString)
    fun out(str: Any) = out(+str)

    fun outNl(str: TextString)
    fun outNl(str: Any = "") = outNl(+str)

    fun release()
}

internal fun textCharacter(character: Char): TextCharacterWrapper = TextCharacterWrapper(character)

operator fun (() -> Any).unaryPlus(): TextString =
    { this().toString().map { { textCharacter(it) } }.toTypedArray() }

operator fun Any.unaryPlus(): TextString = {
    toString().map { { textCharacter(it) } }.toTypedArray()
}

operator fun TextString.plus(other: TextString): TextString = { invoke() + other() }

operator fun TextString.plus(other: List<TextString>): TextString = {
    invoke() + other.flatMap { it().toList() }
}

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