@file:Suppress("unused", "ClassName", "PropertyName")

package dev.zieger.utils.log

import dev.zieger.utils.log.LogLevel.*
import dev.zieger.utils.misc.cast
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope


/**
 * Log-Scope
 */
interface ILogContext : ILogSettings, ILogTags, ILogCalls, ICoroutineLogCalls, IInlineLogCalls, IInlineLogBuilder,
    ILogFilters, ILogMessageBuilder, ILogOutput, ILogPreHook {

    var tag: String?
        get() = tags.lastOrNull()
        set(value) {
            value?.addTag()
        }

    fun ILogContext.copy(
        settings: ILogSettings = cast<ILogSettings>().copy(),
        tags: ILogTags = this,
        filter: ILogFilters = LogFilters(ArrayList(filters)),
        builder: ILogMessageBuilder = this,
        output: ILogOutput = this,
        preHook: ILogPreHook = this
    ): ILogContext = LogContext(settings, tags, filter, builder, output, preHook)
}

open class LogContext(
    logSettings: ILogSettings = LogSettings(),
    logTags: ILogTags = LogTags(),
    logFilter: ILogFilters = +LogLevelFilter,
    logMsgBuilder: ILogMessageBuilder = LogElementMessageBuilder(),
    logOutput: ILogOutput = SystemPrintOutput,
    logPreHook: ILogPreHook = EmptyLogPreHook
) : ILogContext, ILogSettings by logSettings, ILogTags by logTags, ILogFilters by logFilter,
    ILogMessageBuilder by logMsgBuilder, ILogOutput by logOutput, ILogPreHook by logPreHook {

    protected open fun ILogMessageContext.out(msg: String) {
        message = msg
        val outputMessage = build(msg)
        messageFilter.filterWithAction(this) { onPreHook(outputMessage) }
        (filters + messageFilter).filterWithAction(this) { write(outputMessage) }
    }

    private fun List<ILogFilter>.filterWithAction(
        context: ILogMessageContext,
        action: () -> Unit
    ) = context.apply {
        val lambdas = ArrayList<() -> Unit>()
        var idx = 0
        for (filter in reversed()) {
            val lambda = when (idx++) {
                0 -> action
                else -> lambdas[idx - 2]
            }
            lambdas.add {
                filter.run {
                    filter { lambda() }
                }
            }
        }
        (lambdas.lastOrNull() ?: action).invoke()
    }


    /**
     * Log-Calls
     */
    override fun v(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(VERBOSE, tags = tags + tag, messageFilter = +filter).out(msg)

    override fun d(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(DEBUG, tags = tags + tag, messageFilter = +filter).out(msg)

    override fun i(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(INFO, tags = tags + tag, messageFilter = +filter).out(msg)

    override fun w(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(WARNING, tags = tags + tag, messageFilter = +filter).out(msg)

    override fun e(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(EXCEPTION, tags = tags + tag, messageFilter = +filter).out(msg)

    override fun e(throwable: Throwable, msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(EXCEPTION, throwable = throwable, messageFilter = +filter, tags = tags + tag).out(msg)


    /**
     * Log-Coroutine-Calls
     */
    override fun CoroutineScope.v(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(VERBOSE, tags = tags + tag, messageFilter = +filter, coroutineScope = this).out(msg)

    override fun CoroutineScope.d(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(DEBUG, tags = tags + tag, messageFilter = +filter, coroutineScope = this).out(msg)

    override fun CoroutineScope.i(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(INFO, tags = tags + tag, messageFilter = +filter, coroutineScope = this).out(msg)

    override fun CoroutineScope.w(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(WARNING, tags = tags + tag, messageFilter = +filter, coroutineScope = this).out(msg)

    override fun CoroutineScope.e(msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(EXCEPTION, tags = tags + tag, messageFilter = +filter, coroutineScope = this).out(msg)

    override fun CoroutineScope.e(throwable: Throwable, msg: String, vararg tag: String, filter: ILogFilter?) =
        messageContext(
            EXCEPTION, throwable = throwable, messageFilter = +filter, coroutineScope = this,
            tags = tags
        ).out(msg)

    /**
     * Log-Inline-Calls
     */
    override fun <T> T.logV(msg: String, vararg tag: String, filter: ILogFilter?): T =
        apply { messageContext(VERBOSE, tags = tags + tag, messageFilter = +filter).out(msg) }

    override fun <T> T.logD(msg: String, vararg tag: String, filter: ILogFilter?): T =
        apply { messageContext(DEBUG, tags = tags + tag, messageFilter = +filter).out(msg) }

    override fun <T> T.logI(msg: String, vararg tag: String, filter: ILogFilter?): T =
        apply { messageContext(INFO, tags = tags + tag, messageFilter = +filter).out(msg) }

    override fun <T> T.logW(msg: String, vararg tag: String, filter: ILogFilter?): T =
        apply { messageContext(WARNING, tags = tags + tag, messageFilter = +filter).out(msg) }

    override fun <T> T.logE(msg: String, vararg tag: String, filter: ILogFilter?): T =
        apply { messageContext(EXCEPTION, tags = tags + tag, messageFilter = +filter).out(msg) }

    override fun <T> T.logE(throwable: Throwable, msg: String, vararg tag: String, filter: ILogFilter?): T =
        apply { messageContext(EXCEPTION, throwable = throwable, messageFilter = +filter, tags = tags + tag).out(msg) }

    /**
     * Log-Inline-Builder-Calls
     */
    override infix fun <T> T.logV(msg: ILogMessageContext.(T) -> String): T =
        apply { messageContext(VERBOSE).run { out(msg(this@apply)) } }

    override infix fun <T> T.logD(msg: ILogMessageContext.(T) -> String): T =
        apply { messageContext(DEBUG).run { out(msg(this@apply)) } }

    override infix fun <T> T.logI(msg: ILogMessageContext.(T) -> String): T =
        apply { messageContext(INFO).run { out(msg(this@apply)) } }

    override infix fun <T> T.logW(msg: ILogMessageContext.(T) -> String): T =
        apply { messageContext(WARNING).run { out(msg(this@apply)) } }

    override infix fun <T> T.logE(msg: ILogMessageContext.(T) -> String): T =
        apply { messageContext(EXCEPTION).run { out(msg(this@apply)) } }
}

interface IGlobalLogContext : ILogContext {
    fun configure(
        settings: ILogSettings = cast<ILogSettings>().copy(),
        tags: ILogTags = this,
        filter: ILogFilters = cast<ILogFilters>().copy(),
        builder: ILogMessageBuilder = this,
        output: ILogOutput = this,
        preHook: ILogPreHook = this
    ): IGlobalLogContext = object : IGlobalLogContext, ILogContext by copy(settings, tags, filter, builder, output) {}
}

val Log get() = LogScope.Log

var LogScope: ILogScope = LogScopeImpl()
    internal set

/**
 * Log-Message-Context
 */
interface ILogMessageContext : ILogContext {
    var level: LogLevel
    var throwable: Throwable?
    var coroutineScope: CoroutineScope?
    val createdAt: ITimeEx
    var message: String
    val messageFilter: MutableList<ILogFilter>
}

private class LogMessageContext(
    logContext: ILogContext,
    override var level: LogLevel,
    override var throwable: Throwable? = null,
    override var coroutineScope: CoroutineScope? = null,
    override var message: String = "",
    override val messageFilter: MutableList<ILogFilter> = ArrayList(),
    override val createdAt: ITimeEx = TimeEx()
) : ILogContext by logContext, ILogMessageContext

private fun ILogContext.messageContext(
    level: LogLevel,
    message: String = "",
    throwable: Throwable? = null,
    coroutineScope: CoroutineScope? = null,
    tags: Set<String> = this@messageContext.tags,
    messageFilter: MutableList<ILogFilter> = ArrayList()
): ILogMessageContext =
    LogMessageContext(
        copy(tags = LogTags(tags.toMutableSet())),
        level, throwable, coroutineScope, message, messageFilter
    )