@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.gapps.utils.log

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.log.Filter.Companion.CONTENT
import de.gapps.utils.log.Filter.Companion.EXTERNAL
import de.gapps.utils.log.Filter.Companion.EXTERNAL.Companion.ExternalReturn.OK
import de.gapps.utils.log.Filter.Companion.EXTERNAL.Companion.ExternalReturn.RECHECK
import de.gapps.utils.log.Filter.Companion.INTERVAL
import de.gapps.utils.log.Filter.Companion.NONE
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.minus
import de.gapps.utils.time.base.plus
import de.gapps.utils.time.base.rem
import de.gapps.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


sealed class Filter(val id: String) {
    companion object {
        object NONE : Filter("")
        class CONTENT(id: String) : Filter(id)
        class INTERVAL(id: String, val duration: IDurationEx, val resend: Boolean = true) : Filter(id)
        class EXTERNAL(id: String, val callback: (LogLevel?, String) -> ExternalReturn) : Filter(id) {
            companion object {
                sealed class ExternalReturn {
                    object OK : ExternalReturn()
                    object DENY : ExternalReturn()
                    class RECHECK(val duration: IDurationEx) : ExternalReturn()
                }
            }
        }
    }
}

internal class LogFilterProxy(private val scope: DefaultCoroutineScope = DefaultCoroutineScope()) {

    private val previousContentMessages = HashMap<String, Pair<LogLevel?, String>>()
    private val previousIntervalMessages = HashMap<String, Triple<ITimeEx, LogLevel?, String>>()
    private val previousIntervalMessagesResend = HashMap<String, Triple<Job, LogLevel?, String>>()

    fun filterMessage(level: LogLevel?, msg: String, filter: Filter = NONE, action: (LogLevel?, String) -> Unit) {
        when (filter) {
            NONE -> action(level, msg)
            is CONTENT -> if (previousContentMessages[filter.id] != level to msg) {
                previousContentMessages[filter.id] = level to msg
                action(level, msg)
            }
            is INTERVAL -> previousIntervalMessages[filter.id]?.also { prev ->
                val now = TimeEx()
                val allowedSince = prev.first - prev.first % filter.duration + filter.duration
                if (filter.resend) {
                    previousIntervalMessagesResend[filter.id]?.first?.cancel()
                    if (now < allowedSince)
                        previousIntervalMessagesResend[filter.id] = scope.launchEx(delayed = allowedSince - now) {
                            action(level, msg)
                        } to level to msg
                    else action(level, msg)
                } else if (now >= allowedSince) {
                    previousIntervalMessages[filter.id] = now to prev.second to prev.third
                    action(prev.second, prev.third)
                }
            }
            is EXTERNAL -> {
                when (val externalReturn = filter.callback(level, msg)) {
                    OK -> action(level, msg)
                    is RECHECK -> scope.launchEx(delayed = externalReturn.duration) {
                        filterMessage(level, msg, filter, action)
                    }
                    else -> Unit
                }
            }
        }
    }
}

object Log {

    private val builder = MessageBuilder

    fun CoroutineScope.v(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.VERBOSE,
        (builder.run { wrapMessage("V", msg) }),
        filter
    )

    fun v(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.VERBOSE,
        (builder.wrapMessage(null, "V", msg)),
        filter
    )

    fun CoroutineScope.d(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.DEBUG,
        (builder.run { wrapMessage("D", msg) }),
        filter
    )

    fun d(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.DEBUG,
        (builder.wrapMessage(null, "D", msg)),
        filter
    )

    fun CoroutineScope.i(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.INFO,
        (builder.run { wrapMessage("I", msg) }),
        filter
    )

    fun i(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.INFO,
        (builder.wrapMessage(null, "I", msg)),
        filter
    )

    fun CoroutineScope.w(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.WARNING,
        (builder.run { wrapMessage("W", msg) }),
        filter
    )

    fun w(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.WARNING,
        (builder.wrapMessage(null, "W", msg)),
        filter
    )

    fun CoroutineScope.e(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E", msg) }),
        filter
    )

    fun e(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E", msg)),
        filter
    )

    fun CoroutineScope.e(t: Throwable, msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E", "$t $msg") }),
        filter
    )

    fun e(t: Throwable, msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E", "$t $msg")),
        filter
    )

    fun r(msg: String = "", filter: Filter = NONE) = out(null, msg, filter)

    var logLevel: LogLevel = LogLevel.VERBOSE

    private val filterProxy = LogFilterProxy()

    private val out: (level: LogLevel?, msg: String, filter: Filter) -> Unit =
        { level, msg, filter ->
            val action: (level: LogLevel?, msg: String) -> Unit = { l, m -> elements.all { it.log(l, m) } }
            filterProxy.filterMessage(level, msg, filter, action)
        }

    internal val elements = ArrayList(listOf(LogLevelFilter, PrintLn))

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

