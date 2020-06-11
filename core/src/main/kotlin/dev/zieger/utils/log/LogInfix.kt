@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.log


inline infix fun <T : Any?> T.logV(crossinline block: ILogMessageContext.(T) -> String) = apply {
    Log.run { logV { block(this@apply) } }
}

inline infix fun <T : Any?> T.logD(crossinline block: ILogMessageContext.(T) -> String) = apply {
    Log.run { logD { block(this@apply) } }
}

inline infix fun <T : Any?> T.logI(crossinline block: ILogMessageContext.(T) -> String) = apply {
    Log.run { logI { block(this@apply) } }
}

inline infix fun <T : Any?> T.logW(crossinline block: ILogMessageContext.(T) -> String) = apply {
    Log.run { logW { block(this@apply) } }
}

inline infix fun <T : Any?> T.logE(crossinline block: ILogMessageContext.(T) -> String) = apply {
    Log.run { logE { block(this@apply) } }
}

infix fun <T : Any?> T.logV(msg: String) = apply { Log.v(msg) }
infix fun <T : Any?> T.logD(msg: String) = apply { Log.d(msg) }
infix fun <T : Any?> T.logI(msg: String) = apply { Log.i(msg) }
infix fun <T : Any?> T.logW(msg: String) = apply { Log.w(msg) }
infix fun <T : Any?> T.logE(msg: String) = apply { Log.e(msg) }

fun <T : Any?> T.logV(msg: String, logElement: ILogElement? = null) = apply { Log.v(msg, element = logElement) }
fun <T : Any?> T.logD(msg: String, logElement: ILogElement? = null) = apply { Log.d(msg, element = logElement) }
fun <T : Any?> T.logI(msg: String, logElement: ILogElement? = null) = apply { Log.i(msg, element = logElement) }
fun <T : Any?> T.logW(msg: String, logElement: ILogElement? = null) = apply { Log.w(msg, element = logElement) }
fun <T : Any?> T.logE(msg: String, logElement: ILogElement? = null) = apply { Log.e(msg, element = logElement) }