@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.console

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import kotlinx.coroutines.*

class Console(
    vararg components: ConsoleComponent = arrayOf(ConsoleComponent()),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConsoleOptions = ConsoleOptions(),
    block: suspend ConsoleOwnerScope.() -> Unit
) {

    private val screen: Screen = DefaultTerminalFactory()
        .setTerminalEmulatorTitle(options.title)
        .createScreen()

    private val windowManager = MultiWindowTextGUI(screen)

    init {
        ConsoleWindow(options, scope).run {
            screen.startScreen()
            windowManager.addWindow(this)
            components.forEach {
                addConsoleComponent(it)
            }
            scope.launch {
                while (isActive) {
                    suspendCancellableCoroutine<KeyStroke?> {
                        it.resume(textGUI?.screen?.readInput()) { t ->
                            System.err.println(t)
                        }
                    }?.let { onKeyPressed(it) }
                }
            }
            scope.launch { block(this@run) }
            windowManager.waitForWindowToClose(this)
        }
    }
}