package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel
import kotlinx.coroutines.CoroutineScope

interface ILogOut {

    fun out(
        lvl: LogLevel,
        msg: Any,
        filter: LogFilter,
        throwable: Throwable? = null,
        scope: CoroutineScope? = null,
        tag: Any? = null
    )
}