package dev.zieger.utils.gui.console

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.misc.nullWhenEmpty

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
                def.createComponent(this, this@scope, window).onEach { c -> addComponent(c) }
            }.filterIsInstance<ConsoleComponent>().nullWhenEmpty()?.also { consoles ->
                lastConsoleScope = ConsoleScope(this@scope, consoles).apply { launchEx { block() } }
            }
        }
    }.join()
}