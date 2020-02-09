@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.log

import de.gapps.utils.log.Filter.Companion.NONE

open class LogContext(
    var filter: Filter = NONE,
    var message: String = ""
) {
    var f: Filter
        get() = filter
        set(value) {
            filter = value
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

fun <T : Any?> T.logV(msg: String, filter: Filter = NONE) = apply { Log.v(msg, filter) }
fun <T : Any?> T.logD(msg: String, filter: Filter = NONE) = apply { Log.d(msg, filter) }
fun <T : Any?> T.logI(msg: String, filter: Filter = NONE) = apply { Log.i(msg, filter) }
fun <T : Any?> T.logW(msg: String, filter: Filter = NONE) = apply { Log.w(msg, filter) }
fun <T : Any?> T.logE(msg: String, filter: Filter = NONE) = apply { Log.e(msg, filter) }