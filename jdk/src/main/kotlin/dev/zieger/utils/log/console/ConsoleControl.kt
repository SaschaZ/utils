@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.log.console

import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.coroutines.runCommand
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

    private val FULL_LINE_CHARACTER_RANGE = 0..80

    suspend fun flush() = executeNativeBlocking { System.out.flush() }

    fun backspace(num: Int = 1, stream: PrintStream = System.out) = BS(num)
    fun carriageReturn(stream: PrintStream = System.out) = CR()
    fun horizontalTab(num: Int = 1, stream: PrintStream = System.out) = HT(num)

    fun clearLine(stream: PrintStream = System.out) =
        stream.print("${CR.character}\r${FULL_LINE_CHARACTER_RANGE.joinToString("") { " " }}${CR.character}\r")

    suspend fun clearScreen() = "clear".runCommand()
}