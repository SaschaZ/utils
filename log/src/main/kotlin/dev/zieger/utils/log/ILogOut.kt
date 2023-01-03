package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.time.TimeStamp
import kotlinx.coroutines.CoroutineScope

interface ILogOut {

    fun out(
        lvl: LogLevel,
        msg: Any,
        throwable: Throwable? = null,
        scope: CoroutineScope? = null,
        tag: Any? = null
    )
}


class LogOut(
    private val logQueue: ILogQueue,
    private val logTag: ILogTag
) : ILogOut {

    override fun out(
        lvl: LogLevel,
        msg: Any,
        throwable: Throwable?,
        scope: CoroutineScope?,
        tag: Any?
    ) = LogMessageContext(
        logQueue, lvl, throwable, msg, scope, TimeStamp(),
        messageTag = tag, tag = logTag.tag,
    ).run { execute() }
}
