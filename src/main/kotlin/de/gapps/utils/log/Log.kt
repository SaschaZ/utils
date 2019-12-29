@file:Suppress("unused")

package de.gapps.utils.log

import com.github.ajalt.mordant.TermColors
import de.gapps.utils.UtilsSettings
import kotlinx.coroutines.CoroutineScope

object Log {

    private val builder = MessageBuilder

    fun CoroutineScope.v(msg: String) = out(
        LogLevel.VERBOSE,
        (builder.run { wrapMessage("V" to msg) })
    )

    fun v(msg: String) = out(
        LogLevel.VERBOSE,
        (builder.wrapMessage(null, "V" to msg))
    )

    fun CoroutineScope.d(msg: String) = out(
        LogLevel.DEBUG,
        (builder.run { wrapMessage("D" to msg) })
    )

    fun d(msg: String) = out(
        LogLevel.DEBUG,
        (builder.wrapMessage(null, "D" to msg))
    )

    fun CoroutineScope.i(msg: String) = out(
        LogLevel.INFO,
        (builder.run { wrapMessage("I" to msg) })
    )

    fun i(msg: String) = out(
        LogLevel.INFO,
        (builder.wrapMessage(null, "I" to msg))
    )

    fun CoroutineScope.w(msg: String) = out(
        LogLevel.WARNING,
        (builder.run { wrapMessage("W" to msg) })
    )

    fun w(msg: String) = out(
        LogLevel.WARNING,
        (builder.wrapMessage(null, "W" to msg))
    )

    fun CoroutineScope.e(msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E" to msg) })
    )

    fun e(msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E" to msg))
    )

    fun CoroutineScope.e(t: Throwable, msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E" to "$t $msg") })
    )

    fun e(t: Throwable, msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E" to "$t $msg"))
    )

    private fun out(level: LogLevel, msg: String) {
        if (level.ordinal < UtilsSettings.LOG_LEVEL.ordinal) return

        with(TermColors()) {
            println(
                when (level) {
                    LogLevel.VERBOSE -> white(msg)
                    LogLevel.DEBUG -> brightBlue(msg)
                    LogLevel.INFO -> brightCyan(msg)
                    LogLevel.WARNING -> brightYellow(msg)
                    LogLevel.EXCEPTION -> brightRed(msg)
                }
            )
        }
    }
}

