package dev.zieger.utils.log.calls

import dev.zieger.utils.log.ILogMessageContext
import dev.zieger.utils.log.ILogQueue
import dev.zieger.utils.log.ILogTag
import dev.zieger.utils.log.LogMessageContext
import dev.zieger.utils.log.filter.LogLevel

interface ILogReceiverBuilder {
    infix fun <T> T.logV(builder: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logD(builder: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logI(builder: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logW(builder: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logE(builder: ILogMessageContext.(T) -> Any): T
}

class LogReceiverBuilder(
    private val queue: ILogQueue,
    private val tags: ILogTag
) : ILogReceiverBuilder {

    override infix fun <T> T.logV(builder: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(queue, LogLevel.VERBOSE, tag = tags.tag).run {
            message = builder(this@apply)
            execute()
        }
    }

    override infix fun <T> T.logD(builder: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(queue, LogLevel.DEBUG, tag = tags.tag).run {
            message = builder(this@apply)
            execute()
        }
    }

    override infix fun <T> T.logI(builder: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(queue, LogLevel.INFO, tag = tags.tag).run {
            message = builder(this@apply)
            execute()
        }
    }

    override infix fun <T> T.logW(builder: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(queue, LogLevel.WARNING, tag = tags.tag).run {
            message = builder(this@apply)
            execute()
        }
    }

    override infix fun <T> T.logE(builder: ILogMessageContext.(T) -> Any): T = apply {
        LogMessageContext(queue, LogLevel.EXCEPTION, tag = tags.tag).run {
            message = builder(this@apply)
            execute()
        }
    }
}