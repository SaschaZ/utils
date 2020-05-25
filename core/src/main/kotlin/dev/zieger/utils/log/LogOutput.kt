package dev.zieger.utils.log

/**
 * Log-Output
 */
interface ILogOutput {
    fun ILogMessageContext.write(msg: String)
}

object SystemPrintOutput : ILogOutput {
    override fun ILogMessageContext.write(msg: String) = println(msg)
}