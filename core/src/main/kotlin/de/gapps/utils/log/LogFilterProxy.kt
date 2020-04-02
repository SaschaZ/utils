package de.gapps.utils.log

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.log.LogFilter.Companion.EXTERNAL
import de.gapps.utils.log.LogFilter.Companion.EXTERNAL.Companion.ExternalReturn.OK
import de.gapps.utils.log.LogFilter.Companion.EXTERNAL.Companion.ExternalReturn.RECHECK
import de.gapps.utils.log.LogFilter.Companion.GENERIC
import de.gapps.utils.log.LogFilter.Companion.NONE
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.minus
import de.gapps.utils.time.base.plus
import de.gapps.utils.time.duration.milliseconds
import kotlinx.coroutines.Job

internal class LogFilterProxy(private val scope: DefaultCoroutineScope = DefaultCoroutineScope()) {

    private val previousContentMessages = HashMap<String, Pair<LogLevel?, String>>()
    private val previousIntervalMessages = HashMap<String, Triple<ITimeEx, LogLevel?, String>>()
    private val previousIntervalMessagesResend = HashMap<String, Triple<Job, LogLevel?, String>>()

    fun filterMessage(
        level: LogLevel?,
        msg: String,
        logFilter: LogFilter = NONE,
        action: (LogLevel?, String) -> Unit
    ) {
        when (logFilter) {
            NONE -> action(level, msg)
            is GENERIC -> {
                if (logFilter.disableLog) return

                val contentChanged = !logFilter.onlyOnContentChange
                        || previousContentMessages[logFilter.id]?.second?.let { it != msg } != false

                val now = TimeEx()
                val minInterval = logFilter.minInterval ?: 1.milliseconds
                val toWait = previousIntervalMessages[logFilter.id]?.first.let { last ->
                    last?.let { last - now + minInterval }
                }

                fun act(level: LogLevel?, msg: String) {
                    action(level, msg)
                    previousContentMessages[logFilter.id] = level to msg
                    previousIntervalMessages[logFilter.id] = now to level to msg
                    previousIntervalMessagesResend.remove(logFilter.id)
                }

                if (contentChanged) {
                    if (toWait?.negative != false) act(level, msg)
                    else if (logFilter.resend) {
                        previousIntervalMessagesResend[logFilter.id]?.first?.cancel()
                        previousIntervalMessagesResend[logFilter.id] =
                            scope.launchEx(delayed = toWait) { act(level, msg) } to level to msg
                    }
                }
            }
            is EXTERNAL -> {
                when (val externalReturn = logFilter.callback(level, msg)) {
                    OK -> action(level, msg)
                    is RECHECK -> scope.launchEx(delayed = externalReturn.duration) {
                        filterMessage(level, msg, logFilter, action)
                    }
                    else -> Unit
                }
            }
        }
    }
}