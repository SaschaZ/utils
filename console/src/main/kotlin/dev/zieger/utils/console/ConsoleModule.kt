package dev.zieger.utils.console

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import dev.zieger.utils.console.ConsoleInstances.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.Executors

enum class ConsoleInstances(qualifier: Qualifier) : Qualifier by qualifier {
    UI_SCOPE(named("UiScope")),
    INPUT_SCOPE(named("InputScope")),
    SIZE_SCOPE(named("SizeScope")),
    PROCESS_SCOPE(named("ProcessScope"))
}

fun consoleModule(
    options: ConsoleOptions,
    components: List<FocusableComponent>
) = module {

    single(UI_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }
    single(SIZE_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }
    single(INPUT_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }
    single(PROCESS_SCOPE) { CoroutineScope(Dispatchers.IO) }

    single { options }
    single<Screen> { DefaultTerminalFactory().setTerminalEmulatorTitle(options.title).createScreen() }
    single { MultiWindowTextGUI(get()) }

    single { ConsoleWindow(get(), get()) }
}