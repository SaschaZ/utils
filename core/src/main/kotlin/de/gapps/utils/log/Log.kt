@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.gapps.utils.log

import kotlinx.coroutines.CoroutineScope

object Log {

    private val builder = MessageBuilder

    fun CoroutineScope.v(msg: String) = out(
        LogLevel.VERBOSE,
        (builder.run { wrapMessage("V", msg) })
    )

    fun v(msg: String) = out(
        LogLevel.VERBOSE,
        (builder.wrapMessage(null, "V", msg))
    )

    fun CoroutineScope.d(msg: String) = out(
        LogLevel.DEBUG,
        (builder.run { wrapMessage("D", msg) })
    )

    fun d(msg: String) = out(
        LogLevel.DEBUG,
        (builder.wrapMessage(null, "D", msg))
    )

    fun CoroutineScope.i(msg: String) = out(
        LogLevel.INFO,
        (builder.run { wrapMessage("I", msg) })
    )

    fun i(msg: String) = out(
        LogLevel.INFO,
        (builder.wrapMessage(null, "I", msg))
    )

    fun CoroutineScope.w(msg: String) = out(
        LogLevel.WARNING,
        (builder.run { wrapMessage("W", msg) })
    )

    fun w(msg: String) = out(
        LogLevel.WARNING,
        (builder.wrapMessage(null, "W", msg))
    )

    fun CoroutineScope.e(msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E", msg) })
    )

    fun e(msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E", msg))
    )

    fun CoroutineScope.e(t: Throwable, msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.run { wrapMessage("E", "$t $msg") })
    )

    fun e(t: Throwable, msg: String) = out(
        LogLevel.EXCEPTION,
        (builder.wrapMessage(null, "E", "$t $msg"))
    )

    fun r(msg: String) = out(null, msg)

    var out: (level: LogLevel?, msg: String) -> Unit = { _, msg -> println(msg) }
}

