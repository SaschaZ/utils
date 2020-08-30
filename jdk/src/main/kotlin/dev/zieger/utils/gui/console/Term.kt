package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogLevel
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

abstract class Term(protected open val scope: CoroutineScopeEx = DefaultCoroutineScope()) {

    protected lateinit var window: BasicWindow
    protected lateinit var terminal: Terminal
    protected lateinit var screen: Screen
    protected lateinit var gui: MultiWindowTextGUI

    fun start() = runBlocking {
        Log.logLevel = LogLevel.EXCEPTION

        terminal = executeNativeBlocking { DefaultTerminalFactory().createTerminal() }
        screen = executeNativeBlocking { TerminalScreen(terminal) }
        window = BasicWindow().apply {
            setHints(arrayListOf(Window.Hint.FULL_SCREEN))
            onCreate(this)
        }
        val mainJob = scope.launchEx { onStart(this) }

        executeNativeBlocking { screen.startScreen() }
        gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLACK, TerminalSize.ONE))
        gui.theme = LanternaThemes.getRegisteredTheme("businessmachine")
        gui.addWindowAndWait(window)
        mainJob.cancel()
    }.asUnit()

    protected open suspend fun onStart(scope: CoroutineScope) = Unit

    protected open suspend fun onCreate(window: Window) = Unit
}