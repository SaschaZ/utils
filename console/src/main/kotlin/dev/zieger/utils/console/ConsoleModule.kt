package dev.zieger.utils.console

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import dev.zieger.utils.console.ConsoleInstances.*
import dev.zieger.utils.console.components.FocusableComponent
import dev.zieger.utils.console.dto.ConsoleOptions
import dev.zieger.utils.koin.DiRelease
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.Executors

enum class ConsoleInstances(qualifier: Qualifier) : Qualifier by qualifier {
    UI_SCOPE(named("UiScope")),
    INPUT_SCOPE(named("InputScope")),
    SIZE_SCOPE(named("SizeScope")),
    CLOSE_SCOPE(named("CloseScope")),
    PROCESS_SCOPE(named("ProcessScope"))
}

fun consoleModule(
    options: ConsoleOptions,
    components: List<FocusableComponent>
) = module {

    single(UI_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }
    single(SIZE_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }
    single(INPUT_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }
    single(CLOSE_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }
    single(PROCESS_SCOPE) { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }

    single { options }
    single<Screen> { DefaultTerminalFactory().setTerminalEmulatorTitle(options.title).createScreen() }
    single { MultiWindowTextGUI(get()) }
    single { ConsoleWindow(get(), get()) }

    single {
        DiRelease {
            get<CoroutineScope>(UI_SCOPE).cancel()
            get<CoroutineScope>(SIZE_SCOPE).cancel()
            get<CoroutineScope>(INPUT_SCOPE).cancel()
            get<CoroutineScope>(CLOSE_SCOPE).cancel()
            get<CoroutineScope>(PROCESS_SCOPE).cancel()
        }
    }
}