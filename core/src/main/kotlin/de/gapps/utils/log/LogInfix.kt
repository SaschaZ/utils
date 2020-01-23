package de.gapps.utils.log

inline infix fun <T : Any?> T.logV(block: (T) -> String) = apply {
    Log.v(
        block(this)
    )
}

inline infix fun <T : Any?> T.logD(block: (T) -> String) = apply {
    Log.d(
        block(this)
    )
}

inline infix fun <T : Any?> T.logI(block: (T) -> String) = apply {
    Log.i(
        block(this)
    )
}

inline infix fun <T : Any?> T.logW(block: (T) -> String) = apply {
    Log.w(
        block(this)
    )
}

data class LogEScope(var message: String = "",
                     var throwable: Throwable? = null)

inline infix fun <T: Any?> T.logE(block: LogEScope.(T) -> String) = apply {
    LogEScope().apply {
        val msg = block(this@logE)
        throwable?.also { Log.e(it, msg) } ?: Log.e(msg)
    }
}

infix fun <T : Any?> T.logV(msg: String) = apply { Log.v(msg) }
infix fun <T : Any?> T.logD(msg: String) = apply { Log.d(msg) }
infix fun <T : Any?> T.logI(msg: String) = apply { Log.i(msg) }
infix fun <T : Any?> T.logW(msg: String) = apply { Log.w(msg) }
infix fun <T : Any?> T.logE(msg: String) = apply { Log.e(msg) }