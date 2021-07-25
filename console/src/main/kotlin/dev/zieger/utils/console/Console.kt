@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.console

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.Screen
import dev.zieger.utils.console.ConsoleInstances.INPUT_SCOPE
import dev.zieger.utils.console.ConsoleInstances.PROCESS_SCOPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.get

class Console(
    vararg components: FocusableComponent = arrayOf(ConsoleComponent()),
    options: ConsoleOptions = ConsoleOptions(),
    block: suspend ConsoleOwnerScope.() -> Unit
) {

    private val di = DI(consoleModule(options, components.toList()))

    private val screen: Screen = di.get()

    private val windowManager: MultiWindowTextGUI = di.get()

    init {
        di.get<ConsoleWindow>().run {
            screen.startScreen()
            windowManager.addWindow(this)
            components.forEach {
                addFocusableConsoleComponent(it)
            }
            di.get<CoroutineScope>(INPUT_SCOPE).launch {
                while (isActive) {
                    suspendCancellableCoroutine<KeyStroke?> {
                        it.resume(textGUI?.screen?.readInput()) { t -> System.err.println(t) }
                    }?.let { handleInput(it) }
                }
            }
            di.get<CoroutineScope>(PROCESS_SCOPE).launch { block(this@run) }
            windowManager.waitForWindowToClose(this)
        }
    }
}