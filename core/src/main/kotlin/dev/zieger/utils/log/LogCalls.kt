package dev.zieger.utils.log

import kotlinx.coroutines.CoroutineScope

/**
 * Log-Calls
 */
interface ILogCalls {
    fun v(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun d(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun i(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun w(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun e(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun e(throwable: Throwable, msg: String = "", vararg tag: String = emptyArray(), filter: ILogFilter? = null)
}

interface ICoroutineLogCalls {
    fun CoroutineScope.v(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun CoroutineScope.d(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun CoroutineScope.i(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun CoroutineScope.w(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun CoroutineScope.e(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null)
    fun CoroutineScope.e(
        throwable: Throwable,
        msg: String = "",
        vararg tag: String = emptyArray(),
        filter: ILogFilter? = null
    )
}

interface IInlineLogCalls {
    fun <T> T.logV(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null): T
    fun <T> T.logD(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null): T
    fun <T> T.logI(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null): T
    fun <T> T.logW(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null): T
    fun <T> T.logE(msg: String, vararg tag: String = emptyArray(), filter: ILogFilter? = null): T
    fun <T> T.logE(
        throwable: Throwable,
        msg: String = "$throwable",
        vararg tag: String = emptyArray(),
        filter: ILogFilter? = null
    ): T
}

interface IInlineLogBuilder {
    infix fun <T> T.logV(msg: ILogMessageContext.(T) -> String): T
    infix fun <T> T.logD(msg: ILogMessageContext.(T) -> String): T
    infix fun <T> T.logI(msg: ILogMessageContext.(T) -> String): T
    infix fun <T> T.logW(msg: ILogMessageContext.(T) -> String): T
    infix fun <T> T.logE(msg: ILogMessageContext.(T) -> String): T
}