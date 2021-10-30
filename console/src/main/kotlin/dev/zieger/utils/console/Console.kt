@file:Suppress("EXPERIMENTAL_API_USAGE", "FunctionName")

package dev.zieger.utils.console

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import dev.zieger.utils.console.ConsoleInstances.INPUT_SCOPE
import dev.zieger.utils.console.ConsoleInstances.PROCESS_SCOPE
import dev.zieger.utils.console.components.ConsoleComponent
import dev.zieger.utils.console.components.FocusableComponent
import dev.zieger.utils.console.dto.ConsoleOptions
import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.koin.DI
import dev.zieger.utils.koin.get
import kotlinx.coroutines.*

suspend fun Console(
    vararg components: FocusableComponent = arrayOf(ConsoleComponent()),
    options: ConsoleOptions = ConsoleOptions(),
    wait: Boolean = true,
    block: suspend ConsoleOwnerScope.() -> Unit = {}
): ConsoleOwnerScope {
    val di: DI = DI { arrayOf(consoleModule(options, components.toList())) }
    val screen: Screen = di.get()
    val windowManager: MultiWindowTextGUI = di.get()

    return di.get<ConsoleWindow>().apply {
        executeNativeBlocking { screen.startScreen() }
        windowManager.addWindow(this)
        components.forEach {
            addFocusableConsoleComponent(it)
        }
        di.get<CoroutineScope>(INPUT_SCOPE).launch {
            while (isActive) {
                suspendCancellableCoroutine<KeyStroke?> {
                    it.resume(textGUI?.screen?.readInput()) {}
                }?.let {
                    if (it.keyType == KeyType.EOF)
                        di.release()
                    else handleInput(it)
                }
            }
        }

        suspend fun CoroutineScope.updateUi() {
            while (isActive) {
                executeNativeBlocking { textGUI.updateScreen() }
                delay(options.updateInterval)
            }
        }

        di.get<CoroutineScope>(PROCESS_SCOPE).run {
            if (wait) {
                launch { block(this@apply) }
                updateUi()
            } else {
                launch { updateUi() }
                block(this@apply)
            }
        }
    }
}