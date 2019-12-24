@file:Suppress("unused")

package de.gapps.utils.misc

import de.gapps.utils.UtilsSettings
import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import java.io.OutputStream
import java.io.PrintStream
import kotlin.coroutines.CoroutineContext

inline infix fun <T : Any?> T?.logV(block: (T) -> String) = this?.apply { Log.v(block(this)) }
inline infix fun <T : Any?> T?.logD(block: (T) -> String) = this?.apply { Log.d(block(this)) }
inline infix fun <T : Any?> T?.logI(block: (T) -> String) = this?.apply { Log.i(block(this)) }
inline infix fun <T : Any?> T?.logW(block: (T) -> String) = this?.apply { Log.w(block(this)) }
inline infix fun <T : Any?> T?.logE(block: (T) -> String) = this?.apply { Log.e(block(this)) }

infix fun <T : Any?> T?.logV(msg: String) = apply { Log.v(msg) }
infix fun <T : Any?> T?.logD(msg: String) = apply { Log.d(msg) }
infix fun <T : Any?> T?.logI(msg: String) = apply { Log.i(msg) }
infix fun <T : Any?> T?.logW(msg: String) = apply { Log.w(msg) }
infix fun <T : Any?> T?.logE(msg: String) = apply { Log.e(msg) }

object Log {

    private val out = object : OutputStream() {

        private var sb = StringBuilder()

        private var previousCharWasNl = false
        private var firstPrefixPrinted = false
        private var seqStarted = false

        override fun write(p0: Int) {
            val c = p0.toChar()
            when {
                !firstPrefixPrinted -> {
                    printPrefix()
                    firstPrefixPrinted = true
                }
                previousCharWasNl -> {
                    printPrefix()
                    previousCharWasNl = false
                }
                seqStarted -> {
                    seqStarted = false
                }
                else -> when (c) {
                    '\n' -> {
                        sb.append(c)
                        previousCharWasNl = true
                        flush()
                    }
                    '\\' -> seqStarted = true
                    else -> sb.append(c)
                }
            }
        }

        override fun flush() {
            origOut.write(sb.toString().toByteArray())
            sb.clear()
        }

        override fun close() {
            flush()
        }
    }

    private fun printPrefix() {
        origOut.print("|> ")
    }

    private val origOut = System.out

    init {
        System.setOut(PrintStream(out))
    }

    fun CoroutineScope.v(msg: String) = out(LogLevel.VERBOSE, (wrapMessage("V" to msg)))
    fun v(msg: String) = out(LogLevel.VERBOSE, (wrapMessage(null, "V" to msg)))

    fun CoroutineScope.d(msg: String) = out(LogLevel.DEBUG, (wrapMessage("D" to msg)))
    fun d(msg: String) = out(LogLevel.DEBUG, (wrapMessage(null, "D" to msg)))

    fun CoroutineScope.i(msg: String) = out(LogLevel.INFO, (wrapMessage("I" to msg)))
    fun i(msg: String) = out(LogLevel.INFO, (wrapMessage(null, "I" to msg)))

    fun CoroutineScope.w(msg: String) = out(LogLevel.WARNING, (wrapMessage("W" to msg)))
    fun w(msg: String) = out(LogLevel.WARNING, (wrapMessage(null, "W" to msg)))

    fun CoroutineScope.e(msg: String) = out(LogLevel.EXCEPTION, (wrapMessage("E" to msg)))
    fun e(msg: String) = out(LogLevel.EXCEPTION, (wrapMessage(null, "E" to msg)))
    fun CoroutineScope.e(t: Throwable, msg: String) = out(LogLevel.EXCEPTION, (wrapMessage("E" to "$t $msg")))
    fun e(t: Throwable, msg: String) = out(LogLevel.EXCEPTION, (wrapMessage(null, "E" to "$t $msg")))

    private fun CoroutineScope.wrapMessage(msg: Pair<String, String>) = wrapMessage(coroutineContext, msg)

    private fun wrapMessage(cc: CoroutineContext?, msg: Pair<String, String>, e: Exception = Exception()) =
        "${TimeEx()} - ${msg.first}:${buildParentComponentString(cc, e)}-> ${msg.second}"

    private fun buildParentComponentString(cc: CoroutineContext?, e: Exception): String {
        val (className, methodName) = e.stackTrace.first { it.fileName != "Log.kt" }.run {
            val cn = className.split(".").last().split("$").run { first() to getOrNull(1) }
            val mn = (cn.second ?: methodName).removePrefix("\$suspendImpl")
            Pair(cn.first, mn)
        }
        return "${cc?.let { "${cc[CoroutineName]}:" } ?: ""}$className.$methodName"
    }

    enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        EXCEPTION
    }

    private fun out(level: LogLevel, msg: String) =
        if (level.ordinal >= UtilsSettings.LOG_LEVEL.ordinal) origOut.println(msg) else Unit
}