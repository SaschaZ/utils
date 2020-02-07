@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.gapps.utils.log

import kotlinx.coroutines.CoroutineScope

object Log {

    private val builder = MessageBuilder

    fun CoroutineScope.v(msg: String = "") = out(
        LogLevel.VERBOSE,
        (builder.run { wrapMessage("V", msg) })
    )

    fun v(msg: String = "") = out(
        LogLevel.VERBOSE,
        (builder.wrapMessage(null, "V", msg))
    )

    fun CoroutineScope.d(msg: String = "") = out(
        LogLevel.DEBUG,
        (builder.run { wrapMessage("D", msg) })
    )

    fun d(msg: String = "") = out(
        LogLevel.DEBUG,
        (builder.wrapMessage(null, "D", msg))
    )

    fun CoroutineScope.i(msg: String = "") = out(
        LogLevel.INFO,
        (builder.run { wrapMessage("I", msg) })
    )

    fun i(msg: String = "") = out(
        LogLevel.INFO,
        (builder.wrapMessage(null, "I", msg))
    )

    fun CoroutineScope.w(msg: String = "") = out(
        LogLevel.WARNING,
        (builder.run { wrapMessage("W", msg) })
    )

    fun w(msg: String = "") = out(
        LogLevel.WARNING,
        (builder.wrapMessage(null, "W", msg))
    )

    fun CoroutineScope.e(msg: String = "") = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E", msg) })
    )

    fun e(msg: String = "") = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E", msg))
    )

    fun CoroutineScope.e(t: Throwable, msg: String = "") = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E", "$t $msg") })
    )

    fun e(t: Throwable, msg: String = "") = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E", "$t $msg"))
    )

    fun r(msg: String = "") = out(null, msg)

    var logLevel: LogLevel = LogLevel.VERBOSE

    fun filterLogLevel(level: LogLevel?, block: () -> Unit) {
        if (level?.let { it >= logLevel } != false) block()
    }

    private val out: LogOutput = { level, msg -> elements.all { it.log(level, msg) } }

    internal val elements = ArrayList<LogElement>(listOf(LogLevelFilter, PrintLn))

    operator fun plus(element: LogElement) {
        elements.add(element)
    }

    operator fun minus(element: LogElement) {
        elements.remove(element)
    }

    fun clearElements(
        addLevelFilter: Boolean = false,
        addStdOut: Boolean = false
    ) {
        elements.clear()
        if (addLevelFilter) this + LogLevelFilter
        if (addStdOut) this + PrintLn
    }
}

interface LogElement {
    fun log(level: LogLevel?, msg: String): Boolean
}

object LogLevelFilter : LogElement {
    override fun log(level: LogLevel?, msg: String): Boolean {
        return level?.let { it >= Log.logLevel } != false
    }
}

object PrintLn : LogElement {
    override fun log(level: LogLevel?, msg: String): Boolean {
        println(msg)
        return true
    }
}

typealias LogOutput = (level: LogLevel?, msg: String) -> Unit

