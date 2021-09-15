@file:Suppress("unused", "ClassName", "PropertyName")

package dev.zieger.utils.log


import dev.zieger.utils.log.calls.IInlineLogBuilder
import dev.zieger.utils.log.calls.IInlineLogCalls
import dev.zieger.utils.log.calls.ILogCalls
import dev.zieger.utils.log.filter.ILogLevelFilter
import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.log.filter.LogLevel.*
import dev.zieger.utils.log.filter.LogLevelFilter
import dev.zieger.utils.misc.cast
import dev.zieger.utils.time.TimeStamp
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Context
 */
interface ILogContext : ILogPipeline, ILogTag, ILogLevelFilter,
    ILogCalls, IInlineLogBuilder, IInlineLogCalls {

    fun copy(
        pipeline: ILogPipeline = cast<ILogPipeline>().copyPipeline(),
        tags: ILogTag = cast<ILogTag>().copyTags(),
        logLevelFilter: ILogLevelFilter = cast<ILogLevelFilter>().copyLogLevelFilter(pipeline),
        block: ILogContext.() -> Unit = {}
    ): ILogContext = LogContext(pipeline, tags, logLevelFilter).apply(block)
}

open class LogContext(
    logPipeline: ILogPipeline = LogPipeline(messageBuilder = LogMessageBuilder(), output = SystemPrintOutput),
    logTags: ILogTag = LogTag(),
    logLevelFilter: ILogLevelFilter = LogLevelFilter(logPipeline)
) : ILogOut, ILogContext, ILogPipeline by logPipeline, ILogTag by logTags, ILogLevelFilter by logLevelFilter {

    override fun out(
        lvl: LogLevel,
        msg: Any,
        filter: LogFilter,
        throwable: Throwable?,
        scope: CoroutineScope?,
        tag: Any?
    ) = LogMessageContext(this, lvl, throwable, msg, scope, TimeStamp(), filter, tag ?: this.tag).process()

    /**
     * Log-Inline-Builder-Calls
     */
    override infix fun <T> T.logV(msg: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(this@LogContext, VERBOSE, messageTag = tag).run {
            message = msg(this@apply); process()
        }
    }

    override infix fun <T> T.logD(msg: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(this@LogContext, DEBUG, messageTag = tag).run {
            message = msg(this@apply); process()
        }
    }

    override infix fun <T> T.logI(msg: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(this@LogContext, INFO, messageTag = tag).run {
            message = msg(this@apply); process()
        }
    }

    override infix fun <T> T.logW(msg: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(this@LogContext, WARNING, messageTag = tag).run {
            message = msg(this@apply); process()
        }
    }

    override infix fun <T> T.logE(msg: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(this@LogContext, EXCEPTION, messageTag = tag).run {
            message = msg(this@apply); process()
        }
    }
}
