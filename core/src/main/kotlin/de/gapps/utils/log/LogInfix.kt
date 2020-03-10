@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.log

import de.gapps.utils.log.LogFilter.Companion.NONE

open class LogContext(
    var logFilter: LogFilter = NONE,
    var message: String = ""
) {
    var f: LogFilter
        get() = logFilter
        set(value) {
            logFilter = value
        }

    var m: String
        get() = message
        set(value) {
            message = value
        }
}

inline infix fun <T : Any?> T.logV(block: LogContext.(T) -> Unit) = apply {
    LogContext().run { block(this@logV); Log.v(m, f) }
}

inline infix fun <T : Any?> T.logD(block: LogContext.(T) -> Unit) = apply {
    LogContext().run { block(this@logD); Log.d(m, f) }
}

inline infix fun <T : Any?> T.logI(block: LogContext.(T) -> Unit) = apply {
    LogContext().run { block(this@logI); Log.i(m, f) }
}

inline infix fun <T : Any?> T.logW(block: LogContext.(T) -> Unit) = apply {
    LogContext().run { block(this@logW); Log.w(m, f) }
}

data class LogEContext(
    var throwable: Throwable? = null
) : LogContext() {

    var t: Throwable?
        get() = throwable
        set(value) {
            throwable = value
        }
}

inline infix fun <T : Any?> T.logE(block: LogEContext.(T) -> Unit) = apply {
    LogEContext().apply {
        block(this@logE)
        throwable?.also { Log.e(it, m, f) } ?: Log.e(m, f)
    }
}

infix fun <T : Any?> T.logV(msg: String) = apply { Log.v(msg) }
infix fun <T : Any?> T.logD(msg: String) = apply { Log.d(msg) }
infix fun <T : Any?> T.logI(msg: String) = apply { Log.i(msg) }
infix fun <T : Any?> T.logW(msg: String) = apply { Log.w(msg) }
infix fun <T : Any?> T.logE(msg: String) = apply { Log.e(msg) }

fun <T : Any?> T.logV(msg: String, logFilter: LogFilter = NONE) = apply { Log.v(msg, logFilter) }
fun <T : Any?> T.logD(msg: String, logFilter: LogFilter = NONE) = apply { Log.d(msg, logFilter) }
fun <T : Any?> T.logI(msg: String, logFilter: LogFilter = NONE) = apply { Log.i(msg, logFilter) }
fun <T : Any?> T.logW(msg: String, logFilter: LogFilter = NONE) = apply { Log.w(msg, logFilter) }
fun <T : Any?> T.logE(msg: String, logFilter: LogFilter = NONE) = apply { Log.e(msg, logFilter) }