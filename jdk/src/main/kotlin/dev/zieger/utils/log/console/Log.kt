@file:Suppress("unused")

package dev.zieger.utils.log.console

import com.github.ajalt.mordant.TermColors
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogElement
import dev.zieger.utils.log.LogLevel
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import java.text.DecimalFormat

fun Log.p(
    title: String = "",
    max: Long = 100L,
    updateInterval: IDurationEx = 100.milliseconds,
    unitName: String = "",
    unitSize: Long = 1L,
    showSpeed: Boolean = false,
    scope: CoroutineScope = DefaultCoroutineScope(),
    block: suspend ProgressBar.() -> Unit
) = scope.launchEx {
    ProgressBar(
        title, max, updateInterval.millis.toInt(), System.out, ProgressBarStyle.COLORFUL_UNICODE_BLOCK,
        unitName, unitSize, showSpeed, DecimalFormat("###,###,###,###")
    ).use { it.block() }
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