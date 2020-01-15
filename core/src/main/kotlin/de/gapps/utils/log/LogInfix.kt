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

inline infix fun <T : Any?> T.logE(block: (T) -> String) = apply {
    Log.e(
        block(this)
    )
}

infix fun <T : Any?> T.logV(msg: String) = apply { Log.v(msg) }
infix fun <T : Any?> T.logD(msg: String) = apply { Log.d(msg) }
infix fun <T : Any?> T.logI(msg: String) = apply { Log.i(msg) }
infix fun <T : Any?> T.logW(msg: String) = apply { Log.w(msg) }
infix fun <T : Any?> T.logE(msg: String) = apply { Log.e(msg) }