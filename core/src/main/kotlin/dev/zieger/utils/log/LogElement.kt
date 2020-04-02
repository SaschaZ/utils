package dev.zieger.utils.log

import dev.zieger.utils.misc.nullWhen


object PrintLn : LogElement {
    override fun log(level: LogLevel?, msg: String): String? {
        println(msg)
        return msg
    }
}

interface LogElement {
    fun log(level: LogLevel?, msg: String): String?
}

open class WrapMessage(
    val addTime: Boolean = true,
    private val builder: MessageBuilder = MessageBuilder
) : LogElement {
    override fun log(level: LogLevel?, msg: String) =
        builder.wrapMessage(null, level?.short ?: "", msg, addTime)
}

object LogLevelFilter : LogElement {
    override fun log(level: LogLevel?, msg: String) =
        msg.nullWhen { level?.let { it >= Log.logLevel } == false }
}