@file:Suppress("unused")

package de.gapps.utils.log.console

import com.github.ajalt.mordant.TermColors
import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.log.Log
import de.gapps.utils.log.LogElement
import de.gapps.utils.log.LogLevel
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
        Log + this
    }
}