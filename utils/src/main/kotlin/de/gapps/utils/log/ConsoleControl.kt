@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.log

import de.gapps.utils.coroutines.executeNativeBlocking
import de.gapps.utils.misc.asUnit
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
        print("Boo\n")
        flush()
    }.asUnit()

    suspend fun flush() = executeNativeBlocking { System.out.flush() }

    fun backspace(num: Int = 1) = AsciiControlCharacters.BS(num)
    fun carriageReturn() = AsciiControlCharacters.CR(1)
    fun horizontalTab(num: Int = 1) = AsciiControlCharacters.HT(num)
}