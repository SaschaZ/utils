@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log2

import java.util.*
import kotlin.collections.ArrayList

interface ILogPipeline {
    val preHook: Array<IDelayHook<LogPipelineContext>?>
    val postHook: Array<IDelayHook<LogPipelineContext>?>

    fun ILogMessageContext.process()
}

/**
 *
 */
open class LogPipeline(
    override val preHook: Array<IDelayHook<LogPipelineContext>?> = Array(100) { null },
    var midAction: IHook<LogPipelineContext>,
    override val postHook: Array<IDelayHook<LogPipelineContext>?> = Array(100) { null },
    var endAction: IHook<LogPipelineContext>
) : ILogPipeline {

    override fun ILogMessageContext.process() {
        LogPipelineContext(this).apply {
            hook.run {
                call(hook {
                    preHook.pipeExecute(this, midAction)
                    postHook.pipeExecute(this, endAction)
                })
            }
        }
    }
}

fun <C : ICancellable> Array<IDelayHook<C>?>.pipeExecute(
    context: C,
    endAction: IHook<C>
) {
    val lambdas = LinkedList<IHook<C>>()
    var idx = 0
    for (h in ArrayList(toList().filterNotNull()).reversed()) {
        val lambda = when (idx++) {
            0 -> endAction
            else -> lambdas[idx - 2]
        }
        lambdas.add(hook {
            if (context.isCancelled) return@hook
            h.run {
                call(hook innerHook@{
                    if (context.isCancelled) return@innerHook
                    lambda(this)
                })
            }
        })
    }

    if (!context.isCancelled) (lambdas.lastOrNull() ?: endAction).run { context.call() }
}

interface ICancellable {

    val isCancelled: Boolean
    fun cancel()
}

interface ILogPipelineContext : ILogMessageContext, ICancellable

open class LogPipelineContext(
    messageContext: ILogMessageContext
) : ILogPipelineContext, ILogMessageContext by messageContext {

    override var isCancelled: Boolean = false

    override fun cancel() {
        isCancelled = true
    }
}

inline fun <C: ICancellable> delayHook(crossinline block: C.(next: C.() -> Unit) -> Unit): IDelayHook<C> =
    object : IDelayHook<C> {
        override fun C.call(next: IHook<C>) = block { next(this) }
    }

inline fun <C: ICancellable> hook(crossinline block: C.() -> Unit): IHook<C> = object : IHook<C> {
    override fun C.call() = block()
}

interface IDelayHook<C: ICancellable> {
    fun C.call(next: IHook<C>)
}

interface IHook<C: ICancellable> {
    fun C.call()
    operator fun invoke(context: C) = context.run { call() }
}

open class EmptyHook<C: ICancellable> : IDelayHook<C> {
    override fun C.call(next: IHook<C>) = next(this)
}

object EmptyPipelineLogHook : EmptyHook<LogPipelineContext>()

fun logLevelFilter(minLevel: LogLevel) = delayHook<LogPipelineContext> {
    when {
        level >= minLevel -> it(this)
        else -> cancel()
    }
}