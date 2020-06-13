@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.log.console

import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object ConsoleControl {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("FooWoo")
        println("BooMoo")
        print("DooLoo")
        flush()
        delay(2000)

        backspace(3)

        flush()

        delay(2000)
        print("Boo")
        flush()

        delay(2000)
        carriageReturn()
        print("WooMoo")
        flush()
    }.asUnit()

    suspend fun flush() = executeNativeBlocking { System.out.flush() }

    fun backspace(num: Int = 1) = AsciiControlCharacters.BS(num)
    fun carriageReturn() = AsciiControlCharacters.CR(1)
    fun horizontalTab(num: Int = 1) = AsciiControlCharacters.HT(num)

    fun clearLine() = print("${carriageReturn()}${(0..100).joinToString { " " }}${carriageReturn()}")
    suspend fun clearScreen() = "clear".runCommand()
}