@file:Suppress("unused", "ClassName", "PropertyName")

package dev.zieger.utils.log

import dev.zieger.utils.log.LogLevel.*
import dev.zieger.utils.misc.cast
import kotlinx.coroutines.CoroutineScope


/**
 * Log-Context
 */
interface ILogContext : ILogSettings, ILogTags, ILogElements, ILogMessageBuilder, ILogOutput,
    ILogCalls, ICoroutineLogCalls, IInlineLogCalls, IInlineLogBuilder {

    var tag: String?
        get() = tags.lastOrNull()
        set(value) {
            value?.addTag()
        }

    fun ILogContext.copy(
        settings: ILogSettings = cast<ILogSettings>().copy(),
        tags: ILogTags = this,
        elements: ILogElements = LogElements(ArrayList(this.elements)),
        builder: ILogMessageBuilder = this,
        output: ILogOutput = this
    ): ILogContext = LogContext(settings, tags, builder, elements, output)
}

open class LogContext(
    logSettings: ILogSettings = LogSettings(),
    logTags: ILogTags = LogTags(),
    logMsgBuilder: ILogMessageBuilder = LogElementMessageBuilder(),
    logElements: ILogElements = LogElements(LogLevelElement),
    logOutput: ILogOutput = SystemPrintOutput
) : ILogContext, ILogSettings by logSettings, ILogTags by logTags, ILogElements by logElements,
    ILogMessageBuilder by logMsgBuilder, ILogOutput by logOutput {

    protected open fun ILogMessageContext.out(msg: String) {
        message = msg
        executeElements { write(build(message)) }
    }

    private fun ILogMessageContext.executeElements(
        endAction: ILogMessageContext.() -> Unit
    ) {
        val lambdas = ArrayList<ILogMessageContext.() -> Unit>()
        var idx = 0
        for (filter in ArrayList(elements).reversed()) {
            val lambda = when (idx++) {
                0 -> endAction
                else -> lambdas[idx - 2]
            }
            lambdas.add {
                filter.run {
                    log { lambda() }
                }
            }
        }
        (lambdas.lastOrNull() ?: endAction).invoke(this)
    }


    /**
     * Log-Calls
     */
    override fun v(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(VERBOSE, tags = tags + tag, elements = this + element).out(msg)

    override fun d(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(DEBUG, tags = tags + tag, elements = this + element).out(msg)

    override fun i(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(INFO, tags = tags + tag, elements = this + element).out(msg)

    override fun w(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(WARNING, tags = tags + tag, elements = this + element).out(msg)

    override fun e(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(EXCEPTION, tags = tags + tag, elements = this + element).out(msg)

    override fun e(throwable: Throwable, msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(EXCEPTION, throwable = throwable, tags = tags + tag, elements = this + element).out(msg)


    /**
     * Log-Coroutine-Calls
     */
    override fun CoroutineScope.v(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(VERBOSE, tags = tags + tag, coroutineScope = this, elements = this@LogContext + element).out(msg)

    override fun CoroutineScope.d(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(DEBUG, tags = tags + tag, coroutineScope = this, elements = this@LogContext + element).out(msg)

    override fun CoroutineScope.i(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(INFO, tags = tags + tag, coroutineScope = this, elements = this@LogContext + element).out(msg)

    override fun CoroutineScope.w(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(WARNING, tags = tags + tag, coroutineScope = this, elements = this@LogContext + element).out(msg)

    override fun CoroutineScope.e(msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(EXCEPTION, tags = tags + tag, coroutineScope = this, elements = this@LogContext + element).out(
            msg
        )

    override fun CoroutineScope.e(throwable: Throwable, msg: String, vararg tag: String, element: ILogElement?) =
        messageContext(
            EXCEPTION, throwable = throwable, coroutineScope = this,
            tags = tags, elements = this@LogContext + element
        ).out(msg)

    /**
     * Log-Inline-Calls
     */
    override fun <T> T.logV(msg: String, vararg tag: String, element: ILogElement?): T =
        apply { messageContext(VERBOSE, tags = tags + tag, elements = this@LogContext + element).out(msg) }

    override fun <T> T.logD(msg: String, vararg tag: String, element: ILogElement?): T =
        apply { messageContext(DEBUG, tags = tags + tag, elements = this@LogContext + element).out(msg) }

    override fun <T> T.logI(msg: String, vararg tag: String, element: ILogElement?): T =
        apply { messageContext(INFO, tags = tags + tag, elements = this@LogContext + element).out(msg) }

    override fun <T> T.logW(msg: String, vararg tag: String, element: ILogElement?): T =
        apply { messageContext(WARNING, tags = tags + tag, elements = this@LogContext + element).out(msg) }

    override fun <T> T.logE(msg: String, vararg tag: String, element: ILogElement?): T =
        apply { messageContext(EXCEPTION, tags = tags + tag, elements = this@LogContext + element).out(msg) }

    override fun <T> T.logE(throwable: Throwable, msg: String, vararg tag: String, element: ILogElement?): T =
        apply {
            messageContext(
                EXCEPTION,
                throwable = throwable,
                tags = tags + tag,
                elements = this@LogContext + element
            ).out(msg)
        }

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

val Log get() = LogScope.Log

var LogScope: ILogScope = LogScopeImpl()
    internal set
