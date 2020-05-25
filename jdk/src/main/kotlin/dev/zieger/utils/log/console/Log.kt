@file:Suppress("unused")

package dev.zieger.utils.log.console

import com.github.ajalt.mordant.TermColors
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.*
import kotlinx.coroutines.CoroutineScope
import me.tongfei.progressbar.ProgressBar

fun ILogContext.p(
    title: String = "",
    scope: CoroutineScope = DefaultCoroutineScope(),
    max: Long = 100L,
    block: suspend ProgressBar.() -> Unit
) = scope.launchEx {
    ProgressBar(title, max).use { it.block() }
}

object LogColored : ILogOutput {

    fun initialize() {
        LogScope.configure(output = this)
    }

    override fun ILogMessageContext.write(msg: String) {
        with(TermColors()) {
            println(
                when (this@write.level) {
                    LogLevel.VERBOSE -> brightGreen(msg)
                    LogLevel.DEBUG -> brightBlue(msg)
                    LogLevel.INFO -> brightCyan(msg)
                    LogLevel.WARNING -> brightYellow(msg)
                    LogLevel.EXCEPTION -> brightRed(msg)
                    else -> brightMagenta(msg)
                }
            )
        }
    }
}