@file:Suppress("unused", "ClassName", "PropertyName")

package dev.zieger.utils.log2


import dev.zieger.utils.log2.calls.ICoroutineLogCalls
import dev.zieger.utils.log2.calls.IInlineLogBuilder
import dev.zieger.utils.log2.calls.IInlineLogCalls
import dev.zieger.utils.log2.calls.ILogCalls
import dev.zieger.utils.log2.filter.ILogLevelFilter
import dev.zieger.utils.log2.filter.LogLevel
import dev.zieger.utils.log2.filter.LogLevel.*
import dev.zieger.utils.log2.filter.LogLevelFilter
import dev.zieger.utils.misc.cast
import dev.zieger.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Context
 */
interface ILogContext : ILogPipeline, ILogTags, ILogLevelFilter,
    ILogCalls, ICoroutineLogCalls, IInlineLogCalls, IInlineLogBuilder {

    fun copy(
        pipeline: ILogPipeline = cast<ILogPipeline>().copyPipeline(),
        tags: ILogTags = cast<ILogTags>().copyTags(),
        logLevelFilter: ILogLevelFilter = cast<ILogLevelFilter>().copyLogLevelFilter(pipeline)
    ): ILogContext = LogContext(pipeline, tags, logLevelFilter)
}

inline fun ILogContext.scope(block: ILogScope.() -> Unit) = LogScopeImpl(copy()).block()

open class LogContext(
    logPipeline: ILogPipeline = LogPipeline(messageBuilder = LogMessageBuilder(), output = SystemPrintOutput),
    logTags: ILogTags = LogTags(),
    logLevelFilter: ILogLevelFilter = LogLevelFilter(logPipeline)
) : ILogContext, ILogPipeline by logPipeline, ILogTags by logTags, ILogLevelFilter by logLevelFilter {

    protected open fun out(
        lvl: LogLevel,
        msg: Any,
        filter: LogFilter,
        throwable: Throwable? = null,
        scope: CoroutineScope? = null,
        vararg tag: Any
    ) = LogMessageContext(this, lvl, throwable, msg, scope, TimeEx(), filter, *tag).process()

    /**
     * Log-Calls
     */
    override fun v(msg: Any, vararg tag: Any, filter: LogFilter) =
        out(VERBOSE, msg, filter, null, null, *tag)

    override fun d(msg: Any, vararg tag: Any, filter: LogFilter) =
        out(DEBUG, msg, filter, null, null, *tag)

    override fun i(msg: Any, vararg tag: Any, filter: LogFilter) =
        out(INFO, msg, filter, null, null, *tag)

    override fun w(msg: Any, vararg tag: Any, filter: LogFilter) =
        out(WARNING, msg, filter, null, null, *tag)

    override fun e(msg: Any, vararg tag: Any, filter: LogFilter) =
        out(EXCEPTION, msg, filter, null, null, *tag)

    override fun e(throwable: Throwable, msg: Any, vararg tag: Any, filter: LogFilter) =
        out(EXCEPTION, msg, filter, throwable, null, *tag)


    /**
     * Log-Coroutine-Calls
     */
    override fun CoroutineScope.v(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ) = out(VERBOSE, msg, filter, null, this, *tag)

    override fun CoroutineScope.d(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ) = out(DEBUG, msg, filter, null, this, *tag)

    override fun CoroutineScope.i(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ) = out(INFO, msg, filter, null, this, *tag)

    override fun CoroutineScope.w(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ) = out(WARNING, msg, filter, null, this, *tag)

    override fun CoroutineScope.e(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ) = out(EXCEPTION, msg, filter, null, this, *tag)

    override fun CoroutineScope.e(
        throwable: Throwable,
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ) = out(EXCEPTION, msg, filter, throwable, this, *tag)

    /**
     * Log-Inline-Calls
     */
    override fun <T> T.logV(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ): T = apply { out(VERBOSE, msg, filter, null, null, *tag) }

    override fun <T> T.logD(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ): T = apply { out(DEBUG, msg, filter, null, null, *tag) }

    override fun <T> T.logI(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ): T = apply { out(INFO, msg, filter, null, null, *tag) }

    override fun <T> T.logW(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ): T = apply { out(WARNING, msg, filter, null, null, *tag) }

    override fun <T> T.logE(
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ): T = apply { out(EXCEPTION, msg, filter, null, null, *tag) }

    override fun <T> T.logE(
        throwable: Throwable,
        msg: Any,
        vararg tag: Any,
        filter: LogFilter
    ): T = apply { out(EXCEPTION, msg, filter, null, null, *tag) }

    /**
     * Log-Inline-Builder-Calls
     */
    override infix fun <T> T.logV(msg: ILogMessageContext.(T) -> Any): T =
        apply {
            LogMessageContext(this@LogContext, VERBOSE).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logD(msg: ILogMessageContext.(T) -> Any): T =
        apply {
            LogMessageContext(this@LogContext, DEBUG).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logI(msg: ILogMessageContext.(T) -> Any): T =
        apply {
            LogMessageContext(this@LogContext, INFO).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logW(msg: ILogMessageContext.(T) -> Any): T =
        apply {
            LogMessageContext(this@LogContext, WARNING).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logE(msg: ILogMessageContext.(T) -> Any): T =
        apply {
            LogMessageContext(this@LogContext, EXCEPTION).run {
                message = msg(this@apply); process()
            }
        }
}
