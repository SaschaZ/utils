package dev.zieger.utils.console

import com.googlecode.lanterna.*
import com.googlecode.lanterna.SGR.*
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*

internal class ConsoleTest : AnnotationSpec() {

    @Test
    fun testConsole() = runBlocking {
        Console(
            ConsoleWithCommandComponent(),
            ConsoleComponent(),
            options = ConsoleOptions(
                foreground = WHITE,
                background = BLACK,
                commandForeground = BLACK,
                commandBackground = GREEN,
                outputPrefix = { +"$" * RED / YELLOW_BRIGHT + +" " },
                outputNewLinePrefix = { +"  " },
                commandPrefix = { +":>" * BLACK / GREEN + +" " },
                commandNewLinePrefix = { +"   " }
            ),
            wait = true
        ) {
            out("Your Name: ")
            val name = onInput {
                it.keyType == KeyType.Enter
            }
            outNl(+"Hello " + +"${name?.uppercase(Locale.getDefault())}" * CYAN / BLACK_BRIGHT % BOLD)

            outNl(+"Foo\n\t\tBoo\tDoo\tWoo" * GREEN / YELLOW)
            outNl(+"0" * GREEN / YELLOW)
            outNl(+"1" * GREEN / YELLOW)
            var cnt = 0
            outNl { +"${cnt++}" * GREEN / YELLOW }
            repeat(100) {
                out(+"$it" * GREEN / YELLOW % BOLD % UNDERLINE)
                delay(20)
            }
            outNl("\n$cnt")
            outNl("plain")

            focusedComponent++
            repeat(100) {
                outNl(+"FooBoo $it" * YELLOW / BLUE)
                delay(20)
            }
        }.outNl("LAST")
//        delay(100_000)
    }

    @Test
    fun testScreen() {
        DefaultTerminalFactory().createScreen().run {
            startScreen()
            newTextGraphics().run {
                setCharacter(4, 4, TextCharacter('B', WHITE, BLACK))
                drawRectangle(TerminalPosition(2, 2), TerminalSize(10, 10), 'G')
            }
            setCharacter(5, 5, TextCharacter('F', WHITE, BLACK))
            while (true) {
                refresh()
                doResizeIfNecessary()
                readInput()
            }
        }
    }
}
