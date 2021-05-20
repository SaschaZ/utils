package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import dev.zieger.utils.log2.Log
import java.awt.Font
import java.io.IOException

class PanelScreen(
    layoutManager: LayoutManager,
    screen: TerminalScreen,
    val window: Window
) : Panel(layoutManager), Screen by screen

class CustomSameTextGUIThread(gui: TextGUI) : AbstractTextGUIThread(gui) {

    companion object : TextGUIThreadFactory {
        override fun createTextGUIThread(textGUI: TextGUI): TextGUIThread = CustomSameTextGUIThread(textGUI)
    }

    private val thread = Thread.currentThread()

    override fun getThread(): Thread = thread

    init {
        exceptionHandler = object : TextGUIThread.ExceptionHandler {
            override fun onIOException(e: IOException): Boolean {
                Log.e(e)
                return false
            }

            override fun onRuntimeException(e: RuntimeException): Boolean {
                Log.e(e)
                return false
            }
        }
    }
}

inline fun panel(title: String, fontSize: Int = 14, block: PanelScreen.() -> Unit) {
    require(fontSize > 0)

    DefaultTerminalFactory().setTerminalEmulatorFontConfiguration(
        SwingTerminalFontConfiguration.newInstance(
            Font(Font.MONOSPACED, Font.PLAIN, fontSize)
        )
    ).setTerminalEmulatorTitle(title)
        .createScreen().also { screen ->
            screen.startScreen()
            MultiWindowTextGUI(
                CustomSameTextGUIThread,
                screen, DefaultWindowManager(
                    EmptyWindowDecorationRenderer(),
                    screen.terminalSize
                ), null, EmptySpace(TextColor.ANSI.BLACK, TerminalSize.ZERO)
            ).apply {
                addWindowAndWait(BasicWindow().apply {
                    setHints(listOf(Window.Hint.FULL_SCREEN, Window.Hint.NO_POST_RENDERING))
                    theme = SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK)
                    component = PanelScreen(AbsoluteLayout(), screen, this).apply { block() }
                })
            }
        }
}
