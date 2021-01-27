package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.GridLayout.Alignment.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.gui.console.ConsoleDefinition.*
import dev.zieger.utils.log2.Log
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhenEmpty
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.CoroutineScope
import java.awt.Font
import java.io.IOException

interface IConsoleScope {

    fun out(
        consoleId: Int = 0,
        vararg text: TextWithColor,
        newLine: Boolean = false,
        offset: Int? = null
    ): () -> Unit

    fun out(
        vararg text: TextWithColor,
        offset: Int? = null
    ): () -> Unit = out(0, *text, offset = offset)

    fun outnl(
        consoleId: Int = 0,
        vararg text: TextWithColor,
        offset: Int? = null
    ): () -> Unit = out(consoleId, *text, newLine = true, offset = offset)

    fun outnl(
        vararg text: TextWithColor,
        offset: Int? = null
    ): () -> Unit = out(0, *text, newLine = true, offset = offset)

    fun refresh()
}

data class ConsoleScope(
    private val scope: CoroutineScope,
    private val output: List<ConsoleComponent>
) : CoroutineScope by scope, IConsoleScope {

    override fun out(
        consoleId: Int,
        vararg text: TextWithColor,
        newLine: Boolean,
        offset: Int?
    ): () -> Unit {
        val message = Message(text.toList(), newLine = newLine, offset = offset)
        val consoleComponent = output[consoleId.coerceIn(0..output.lastIndex)]
        consoleComponent.newMessage(message)
        return { consoleComponent.remove(message) }
    }

    override fun refresh() = output.runEach { refresh() }.asUnit()
}

var lastConsoleScope: ConsoleScope? = null

object GlobalConsoleScope : IConsoleScope {
    override fun out(consoleId: Int, vararg text: TextWithColor, newLine: Boolean, offset: Int?): () -> Unit =
        lastConsoleScope?.out(consoleId, *text, newLine = newLine, offset = offset)
            ?: throw IllegalStateException("No console initialized yet")

    override fun refresh() = lastConsoleScope?.refresh()
        ?: throw IllegalStateException("No console initialized yet")
}

suspend inline fun console(
    vararg definition: ConsoleDefinition = arrayOf(ConsoleDefinition()),
    title: String = "Console",
    fontSize: Int = 14,
    crossinline block: suspend ConsoleScope.() -> Unit
) {
    require(fontSize > 0)

    IoCoroutineScope().launchEx scope@{
        panel(title, fontSize) {
            definition.toList().flatMap { def ->
                Log.i("create console #$def")
                def.createComponent(this, this@scope, window).onEach { c -> addComponent(c) }
            }.filterIsInstance<ConsoleComponent>().nullWhenEmpty()?.also { consoles ->
                lastConsoleScope = ConsoleScope(this@scope, consoles).apply { launchEx { block() } }
            }
        }
    }.join()
}

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
                ), null, EmptySpace(BLACK, TerminalSize.ZERO)
            ).apply {
                addWindowAndWait(BasicWindow().apply {
                    setHints(listOf(Window.Hint.FULL_SCREEN, Window.Hint.NO_POST_RENDERING))
                    theme = SimpleTheme(WHITE, BLACK)
                    component = PanelScreen(AbsoluteLayout(), screen, this).apply { block() }
                })
            }
        }
}