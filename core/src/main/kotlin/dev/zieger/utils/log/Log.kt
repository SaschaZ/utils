@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.log

import dev.zieger.utils.log.LogFilter.Companion.NONE
import kotlinx.coroutines.CoroutineScope


object Log {

    fun CoroutineScope.v(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.VERBOSE,
        msg,
        logFilter
    )

    fun v(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.VERBOSE,
        msg,
        logFilter
    )

    fun CoroutineScope.d(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.DEBUG,
        msg,
        logFilter
    )

    fun d(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.DEBUG,
        msg,
        logFilter
    )

    fun CoroutineScope.i(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.INFO,
        msg,
        logFilter
    )

    fun i(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.INFO,
        msg,
        logFilter
    )

    fun CoroutineScope.w(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.WARNING,
        msg,
        logFilter
    )

    fun w(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.WARNING,
        msg,
        logFilter
    )

    fun CoroutineScope.e(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        logFilter
    )

    fun e(msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        logFilter
    )

    fun CoroutineScope.e(t: Throwable, msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        logFilter,
        t
    )

    fun e(t: Throwable, msg: String = "", logFilter: LogFilter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        logFilter,
        t
    )

    fun r(msg: String = "", logFilter: LogFilter = NONE) = out(null, msg, logFilter)

    var logLevel: LogLevel = LogLevel.VERBOSE

    private val filterProxy = LogFilterProxy()

    private fun out(level: LogLevel?, msg: String, logFilter: LogFilter, throwable: Throwable? = null) =
        null.out(level, msg, logFilter, throwable)

    private fun CoroutineScope?.out(level: LogLevel?, msg: String, logFilter: LogFilter, throwable: Throwable? = null) {
        var lastMessage: String? = null
        filterProxy.filterMessage(level, msg, logFilter) { l, m ->
            ArrayList(elements).all {
                lastMessage = it.log(l, lastMessage ?: m)
                lastMessage != null
            }
        }
    }

    internal val elements = ArrayList(listOf(WrapMessage(true), LogLevelFilter, PrintLn))

    operator fun plus(element: LogElement) {
        elements.add(element)
    }

    operator fun minus(element: LogElement) {
        elements.remove(element)
    }

    fun clearElements(
        addWrapper: Boolean = true,
        addTime: Boolean = true,
        addLevelFilter: Boolean = false,
        addStdOut: Boolean = false
    ) {
        elements.clear()
        if (addWrapper) this + WrapMessage(addTime)
        if (addLevelFilter) this + LogLevelFilter
        if (addStdOut) this + PrintLn
    }
}

