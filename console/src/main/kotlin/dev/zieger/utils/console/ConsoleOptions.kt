package dev.zieger.utils.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.Window

data class ConsoleOptions(
    val title: String = "Console",
    val position: TerminalPosition? = null,
    val size: TerminalSize? = null,
    val updateInterval: Long = 50,
    val decoratedSize: TerminalSize = TerminalSize.ZERO,
    val foreground: TextColor = TextColor.ANSI.WHITE,
    val background: TextColor = TextColor.ANSI.BLACK,
    val selectedForeground: TextColor = TextColor.ANSI.BLACK,
    val selectedBackground: TextColor = TextColor.ANSI.WHITE,
    val commandForeground: TextColor = TextColor.ANSI.WHITE,
    val commandBackground: TextColor = TextColor.ANSI.BLACK,
    val hints: List<Window.Hint> = listOf(
        Window.Hint.NO_DECORATIONS,
        Window.Hint.NO_POST_RENDERING,
        Window.Hint.FIT_TERMINAL_WINDOW,
        Window.Hint.FULL_SCREEN
    ),
    val outputPrefix: TextBuilder = {
        +"$ " * foreground / background
    },
    val outputNewLinePrefix: TextBuilder = {
        +"  " * foreground / background
    },
    val commandPrefix: TextBuilder = {
        +"$>" * commandForeground / commandBackground
    },
    val commandNewLinePrefix: TextBuilder = {
        +"  " * commandForeground / commandBackground
    }
)