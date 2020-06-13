@file:Suppress("unused")

package dev.zieger.utils.log.console

import com.github.ajalt.mordant.TermColors
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogElement
import dev.zieger.utils.log.LogLevel
import kotlinx.coroutines.CoroutineScope
import me.tongfei.progressbar.ProgressBar

fun Log.p(
    title: String = "",
    scope: CoroutineScope = DefaultCoroutineScope(),
    max: Long = 100L,
    block: suspend ProgressBar.() -> Unit
) = scope.launchEx {
    ProgressBar(title, max).use { it.block() }
}

object LogColored : LogElement {

    override fun log(level: LogLevel?, msg: String): String? {
        with(TermColors()) {
            println(
                when (level) {
                    LogLevel.VERBOSE -> brightGreen(msg)
                    LogLevel.DEBUG -> brightBlue(msg)
                    LogLevel.INFO -> brightCyan(msg)
                    LogLevel.WARNING -> brightYellow(msg)
                    LogLevel.EXCEPTION -> brightRed(msg)
                    else -> brightMagenta(msg)
                }
            )
        }
        return msg
    }

    fun initialize() {
        Log.clearElements(addLevelFilter = true)
        Log.plusAssign(this)
    }
}