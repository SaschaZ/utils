@file:Suppress("unused", "ClassName", "PropertyName")

package dev.zieger.utils.log2

import dev.zieger.utils.log2.LogLevel.*
import dev.zieger.utils.log2.calls.ICoroutineLogCalls
import dev.zieger.utils.log2.calls.IInlineLogBuilder
import dev.zieger.utils.log2.calls.IInlineLogCalls
import dev.zieger.utils.log2.calls.ILogCalls
import dev.zieger.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Context
 */
interface ILogContext : ILogPipeline, ILogTags, ILogLevelFilter,
    ILogCalls, ICoroutineLogCalls, IInlineLogCalls, IInlineLogBuilder {

    fun copy(
        pipeline: ILogPipeline = this,
        tags: ILogTags = this,
        logLevelFilter: ILogLevelFilter = this
    ): ILogContext = LogContext(pipeline, tags, logLevelFilter)
}

open class LogContext(
    logPipeline: ILogPipeline = LogPipeline(midAction = LogMessageBuilder(), endAction = SystemPrintOutput),
    logTags: ILogTags = LogTags(),
    logLevelFilter: ILogLevelFilter = LogLevelFilter(logPipeline)
) : ILogContext, ILogPipeline by logPipeline, ILogTags by logTags, ILogLevelFilter by logLevelFilter {

    protected open fun out(
        lvl: LogLevel,
        msg: Any,
        hook: IDelayHook<LogPipelineContext>,
        throwable: Throwable? = null,
        scope: CoroutineScope? = null,
        vararg tag: Any
    ) = LogMessageContext(this, lvl, throwable, msg, scope, TimeEx(), hook, *tag).process()

    /**
     * Log-Calls
     */
    override fun v(msg: Any, vararg tag: Any, hook: IDelayHook<LogPipelineContext>) =
        out(VERBOSE, msg, hook, null, null, *tag)

    override fun d(msg: Any, vararg tag: Any, hook: IDelayHook<LogPipelineContext>) =
        out(DEBUG, msg, hook, null, null, *tag)

    override fun i(msg: Any, vararg tag: Any, hook: IDelayHook<LogPipelineContext>) =
        out(INFO, msg, hook, null, null, *tag)

    override fun w(msg: Any, vararg tag: Any, hook: IDelayHook<LogPipelineContext>) =
        out(WARNING, msg, hook, null, null, *tag)

    override fun e(msg: Any, vararg tag: Any, hook: IDelayHook<LogPipelineContext>) =
        out(EXCEPTION, msg, hook, null, null, *tag)

    override fun e(throwable: Throwable, msg: Any, vararg tag: Any, hook: IDelayHook<LogPipelineContext>) =
        out(EXCEPTION, msg, hook, throwable, null, *tag)


    /**
     * Log-Coroutine-Calls
     */
    override fun CoroutineScope.v(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ) = out(VERBOSE, msg, hook, null, this, *tag)

    override fun CoroutineScope.d(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ) = out(DEBUG, msg, hook, null, this, *tag)

    override fun CoroutineScope.i(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ) = out(INFO, msg, hook, null, this, *tag)

    override fun CoroutineScope.w(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ) = out(WARNING, msg, hook, null, this, *tag)

    override fun CoroutineScope.e(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ) = out(EXCEPTION, msg, hook, null, this, *tag)

    override fun CoroutineScope.e(
        throwable: Throwable,
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ) = out(EXCEPTION, msg, hook, throwable, this, *tag)

    /**
     * Log-Inline-Calls
     */
    override fun <T> T.logV(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ): T = apply { out(VERBOSE, msg, hook, null, null, *tag) }

    override fun <T> T.logD(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ): T = apply { out(DEBUG, msg, hook, null, null, *tag) }

    override fun <T> T.logI(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ): T = apply { out(INFO, msg, hook, null, null, *tag) }

    override fun <T> T.logW(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ): T = apply { out(WARNING, msg, hook, null, null, *tag) }

    override fun <T> T.logE(
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ): T = apply { out(EXCEPTION, msg, hook, null, null, *tag) }

    override fun <T> T.logE(
        throwable: Throwable,
        msg: Any,
        vararg tag: Any,
        hook: IDelayHook<LogPipelineContext>
    ): T = apply { out(EXCEPTION, msg, hook, null, null, *tag) }

    /**
     * Log-Inline-Builder-Calls
     */
    override infix fun <T> T.logV(msg: LogPipelineContext.(T) -> Any): T =
        apply {
            LogPipelineContext(LogMessageContext(this@LogContext, VERBOSE)).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logD(msg: LogPipelineContext.(T) -> Any): T =
        apply {
            LogPipelineContext(LogMessageContext(this@LogContext, DEBUG)).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logI(msg: LogPipelineContext.(T) -> Any): T =
        apply {
            LogPipelineContext(LogMessageContext(this@LogContext, INFO)).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logW(msg: LogPipelineContext.(T) -> Any): T =
        apply {
            LogPipelineContext(LogMessageContext(this@LogContext, WARNING)).run {
                message = msg(this@apply); process()
            }
        }

    override infix fun <T> T.logE(msg: LogPipelineContext.(T) -> Any): T =
        apply {
            LogPipelineContext(LogMessageContext(this@LogContext, EXCEPTION)).run {
                message = msg(this@apply); process()
            }
        }
}
