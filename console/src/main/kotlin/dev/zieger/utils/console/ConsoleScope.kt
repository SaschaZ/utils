package dev.zieger.utils.console

import com.googlecode.lanterna.input.KeyStroke

interface ConsoleOwnerScope : ConsoleScope {

    var focusedComponent: Int

    fun out(componentId: Int, builder: TextBuilder)
    fun out(componentId: Int, str: TextString) = out(componentId) { str }
    fun out(componentId: Int, str: Any) = out(componentId, +str)

    fun outNl(componentId: Int, builder: TextBuilder) = out(componentId) { builder() + +"\r" }
    fun outNl(componentId: Int, str: TextString) = outNl(componentId) { str }
    fun outNl(componentId: Int, str: Any) = outNl(componentId, +str)

    override fun out(builder: TextBuilder) = out(focusedComponent, builder)

    suspend fun onInput(
        componentId: Int = focusedComponent,
        printInput: Boolean = true,
        suffix: TextString = +"\n",
        input: ConsoleScope.(KeyStroke) -> Boolean
    ): String?
}

interface ConsoleScope {

    fun out(builder: TextBuilder)
    fun out(str: TextString) = out { str }
    fun out(str: Any = "") = out(+str)

    fun outNl(builder: TextBuilder) = out { builder() + +"\r" }
    fun outNl(str: TextString) = outNl { str }
    fun outNl(str: Any = "") = outNl(+str)

    fun release()
}