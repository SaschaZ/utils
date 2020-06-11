package dev.zieger.utils.log

import dev.zieger.utils.misc.nullWhen


interface LogElement {
    fun log(level: LogLevel?, msg: String): String?
}

object PrintLn : LogElement {
    override fun log(level: LogLevel?, msg: String): String? {
        println(msg)
        return msg
    }
}

open class WrapMessage(
    addTime: Boolean = true,
    printCallOrigin: Boolean = false,
    private val builder: MessageBuilder = MessageBuilder(printCallOrigin, addTime)
) : LogElement {
    override fun log(level: LogLevel?, msg: String) =
        builder.wrapMessage(null, level?.short ?: "", msg)
}

object LogLevelFilter : LogElement {
    override fun log(level: LogLevel?, msg: String) =
        msg.nullWhen { level?.let { it >= Log.logLevel } == false }
}