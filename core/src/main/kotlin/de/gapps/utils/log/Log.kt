@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.gapps.utils.log

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.log.Filter.Companion.EXTERNAL
import de.gapps.utils.log.Filter.Companion.EXTERNAL.Companion.ExternalReturn.OK
import de.gapps.utils.log.Filter.Companion.EXTERNAL.Companion.ExternalReturn.RECHECK
import de.gapps.utils.log.Filter.Companion.GENERIC
import de.gapps.utils.log.Filter.Companion.NONE
import de.gapps.utils.misc.nullWhen
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.minus
import de.gapps.utils.time.base.plus
import de.gapps.utils.time.base.rem
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.years
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


sealed class Filter {
    open val id: String = ""
    open val minInterval: IDurationEx? = null
    open val resend: Boolean = true

    companion object {
        object NONE : Filter()

        data class GENERIC(
            override val id: String,
            override val minInterval: IDurationEx? = null,
            override val resend: Boolean = true,
            val onlyOnContentChange: Boolean = true
        ) : Filter()

        data class EXTERNAL(
            override val id: String,
            val callback: (LogLevel?, String) -> ExternalReturn
        ) : Filter() {
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
            is GENERIC -> {
                val contentChanged = !filter.onlyOnContentChange
                        || previousContentMessages[filter.id]?.second?.let { it != msg } != false

                val now = TimeEx()
                val minInterval = filter.minInterval ?: 1.years
                val intervalOk = previousIntervalMessages[filter.id]?.first.let { last ->
                    last == null || last - last % minInterval + minInterval >= now
                }

                fun act(level: LogLevel?, msg: String) {
                    action(level, msg)
                    previousContentMessages[filter.id] = level to msg
                    previousIntervalMessages[filter.id] = now to level to msg
                    previousIntervalMessagesResend.remove(filter.id)
                }

                if (contentChanged) {
                    if (intervalOk) act(level, msg)
                    else if (filter.resend) {
                        previousIntervalMessagesResend[filter.id]?.first?.cancel()
                        previousIntervalMessagesResend[filter.id] =
                            scope.launchEx { act(level, msg) } to level to msg
                    }
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

    fun CoroutineScope.v(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.VERBOSE,
        msg,
        filter
    )

    fun v(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.VERBOSE,
        msg,
        filter
    )

    fun CoroutineScope.d(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.DEBUG,
        msg,
        filter
    )

    fun d(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.DEBUG,
        msg,
        filter
    )

    fun CoroutineScope.i(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.INFO,
        msg,
        filter
    )

    fun i(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.INFO,
        msg,
        filter
    )

    fun CoroutineScope.w(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.WARNING,
        msg,
        filter
    )

    fun w(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.WARNING,
        msg,
        filter
    )

    fun CoroutineScope.e(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        filter
    )

    fun e(msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        filter
    )

    fun CoroutineScope.e(t: Throwable, msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        filter,
        t
    )

    fun e(t: Throwable, msg: String = "", filter: Filter = NONE) = out(
        LogLevel.EXCEPTION,
        msg,
        filter,
        t
    )

    fun r(msg: String = "", filter: Filter = NONE) = out(null, msg, filter)

    var logLevel: LogLevel = LogLevel.VERBOSE

    private val filterProxy = LogFilterProxy()

    private fun out(level: LogLevel?, msg: String, filter: Filter, throwable: Throwable? = null) =
        null.out(level, msg, filter, throwable)

    private fun CoroutineScope?.out(level: LogLevel?, msg: String, filter: Filter, throwable: Throwable? = null) {
        var lastMessage: String? = null
        filterProxy.filterMessage(level, msg, filter) { l, m ->
            elements.all {
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

interface LogElement {
    fun log(level: LogLevel?, msg: String): String?
}

open class WrapMessage(val addTime: Boolean = true,
                  private val builder: MessageBuilder = MessageBuilder) : LogElement {
    override fun log(level: LogLevel?, msg: String) =
        builder.wrapMessage(null, level?.short ?: "", msg, addTime)
}

object LogLevelFilter : LogElement {
    override fun log(level: LogLevel?, msg: String) =
        msg.nullWhen { level?.let { it >= Log.logLevel } == false }
}

object PrintLn : LogElement {
    override fun log(level: LogLevel?, msg: String): String? {
        println(msg)
        return msg
    }
}

