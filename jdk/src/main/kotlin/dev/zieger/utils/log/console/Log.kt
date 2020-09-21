@file:Suppress("unused")

package dev.zieger.utils.log.console

import com.github.ajalt.mordant.TermColors
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogElement
import dev.zieger.utils.log.LogLevel
import dev.zieger.utils.time.milliseconds

private const val DEFAULT_MAXIMUM_PROGRESS = 100L
private val DEFAULT_UPDATE_INTERVAL = 100.milliseconds

object LogColored : LogElement {

    override fun log(level: LogLevel?, msg: String): String? {
        termColored {
            println(
                when (level) {
                    LogLevel.VERBOSE -> brightBlue(msg)
                    LogLevel.DEBUG -> green(msg)
                    LogLevel.INFO -> yellow(msg)
                    LogLevel.WARNING -> brightMagenta(msg)
                    LogLevel.EXCEPTION -> brightRed(msg)
                    else -> cyan(msg)
                }
            )
        }
        return msg
    }

    fun initialize() {
        Log.clearElements(addLevelFilter = true)
        Log += this
    }
}

inline fun <T> termColored(block: TermColors.() -> T): T = with(TermColors(), block)

