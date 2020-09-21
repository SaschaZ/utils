@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.*


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

fun <T : Any?> T.logV(msg: String, filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter) = apply {
    Log.v(
        msg,
        filter = filter
    )
}

fun <T : Any?> T.logD(msg: String, filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter) =
    apply { Log.d(msg, filter = filter) }

fun <T : Any?> T.logI(msg: String, filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter) =
    apply { Log.i(msg, filter = filter) }

fun <T : Any?> T.logW(msg: String, filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter) =
    apply { Log.w(msg, filter = filter) }

fun <T : Any?> T.logE(msg: String, filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter) =
    apply { Log.e(msg, filter = filter) }