package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

internal class ConsoleTest : AnnotationSpec() {

    @Test
    fun testConsole() = runBlocking {
        Console(
            ConsoleWithCommandComponent(),
            ConsoleComponent(),
            options = ConsoleOptions(
                foreground = TextColor.ANSI.BLACK,
                background = TextColor.ANSI.BLACK_BRIGHT,
                commandForeground = TextColor.ANSI.BLACK,
                commandBackground = TextColor.ANSI.GREEN,
                outputPrefix = +"$ " * TextColor.ANSI.RED / TextColor.ANSI.YELLOW_BRIGHT,
                commandPrefix = +": " * TextColor.ANSI.BLACK / TextColor.ANSI.GREEN
            )
        ) {
            outNl()
            outNl("Foo\n\tBoo" * TextColor.ANSI.GREEN / TextColor.ANSI.YELLOW)
            outNl("0" * TextColor.ANSI.GREEN / TextColor.ANSI.YELLOW)
            outNl("1" * TextColor.ANSI.GREEN / TextColor.ANSI.YELLOW)
            var cnt = 0
            outNl(+{ "${cnt++}" } * TextColor.ANSI.GREEN / { TextColor.ANSI.YELLOW })
            repeat(100) {
                out(+"$it" * TextColor.ANSI.GREEN / TextColor.ANSI.YELLOW)
                delay(50)
            }
            outNl(cnt)
            outNl("plain")

            focusedComponent++
            repeat(100) {
                outNl(+"FooBoo $it" * TextColor.ANSI.YELLOW / TextColor.ANSI.BLUE)
                delay(50)
            }

            delay(100_000)
        }
    }

    @Test
    fun testScreen() {
        DefaultTerminalFactory().createScreen().run {
            startScreen()
            newTextGraphics().run {
                setCharacter(4, 4, TextCharacter('B', TextColor.ANSI.WHITE, TextColor.ANSI.BLACK))
                drawRectangle(TerminalPosition(2, 2), TerminalSize(10, 10), 'G')
            }
            setCharacter(5, 5, TextCharacter('F', TextColor.ANSI.WHITE, TextColor.ANSI.BLACK))
            while (true) {
                refresh()
                doResizeIfNecessary()
                readInput()
            }
        }
    }
}
