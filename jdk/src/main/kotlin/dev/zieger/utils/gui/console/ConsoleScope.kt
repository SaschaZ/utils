package dev.zieger.utils.gui.console

import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.CoroutineScope

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

