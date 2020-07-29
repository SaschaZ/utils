package dev.zieger.utils.log

import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.ITimeEx
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Message-Context
 */
interface ILogMessageContext : ILogContext {
    var level: LogLevel
    var throwable: Throwable?
    var coroutineScope: CoroutineScope?
    var createdAt: ITimeEx
    var message: String
}

fun ILogContext.messageContext(
    level: LogLevel,
    message: String = "",
    throwable: Throwable? = null,
    coroutineScope: CoroutineScope? = null,
    tags: Set<String> = this@messageContext.tags,
    elements: ILogElements = this
): ILogMessageContext =
    LogMessageContext(
        copy(tags = LogTags(tags.toMutableSet()), elements = elements),
        level, throwable, coroutineScope, TimeEx(), message
    )

class LogMessageContext(
    logContext: ILogContext,
    override var level: LogLevel,
    override var throwable: Throwable? = null,
    override var coroutineScope: CoroutineScope? = null,
    override var createdAt: ITimeEx = TimeEx(),
    override var message: String = ""
) : ILogContext by logContext,
    ILogMessageContext