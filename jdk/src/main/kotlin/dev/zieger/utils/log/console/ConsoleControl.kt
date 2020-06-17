@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.log.console

import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.log.console.AsciiControlCharacters.*
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.PrintStream

object ConsoleControl {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("FooWoo")
        println("BooMoo")
        clearScreen()
        print("DooLoo")
        flush()
        delay(2000)

        clearLine()

        flush()

        delay(2000)
        print("Boo")
        flush()

        delay(2000)
        clearLine()
        print("WooMoo")
        flush()
    }.asUnit()

    private const val FULL_LINE_CHARACTER_COUNT = 80

    suspend fun flush() = executeNativeBlocking { System.out.flush() }

    fun backspace(num: Int = 1, stream: PrintStream = System.out) = BS(num)
    fun carriageReturn(stream: PrintStream = System.out) = CR()
    fun horizontalTab(num: Int = 1, stream: PrintStream = System.out) = HT(num)

    fun clearLine(stream: PrintStream = System.out) =
        stream.print("${CR.character}\r${(0..FULL_LINE_CHARACTER_COUNT).joinToString("") { " " }}${CR.character}\r")

    fun clearScreen() = print("CLEAR_CONSOLE \u001b[2J\u001b[H")
}