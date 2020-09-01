@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log2

import dev.zieger.utils.log2.LogHook.LogPostHook
import dev.zieger.utils.log2.LogHook.LogPreHook
import dev.zieger.utils.misc.asUnit
import java.util.*
import kotlin.collections.ArrayList

interface ILogPipeline {
    var messageBuilder: ILogMessageBuilder
    var output: IHook<LogPipelineContext>

    fun addHook(hook: LogHook)
    fun removeHook(hook: LogHook)

    operator fun plusAssign(hook: LogHook) = addHook(hook)
    operator fun minusAssign(hook: LogHook) = removeHook(hook)

    fun ILogMessageContext.process()

    fun copyPipeline(): ILogPipeline
}

/**
 *
 */
open class LogPipeline(
    override var messageBuilder: ILogMessageBuilder,
    override var output: IHook<LogPipelineContext>,
    private val preHook: MutableList<LogPreHook?> = ArrayList(),
    private val postHook: MutableList<LogPostHook?> = ArrayList()
) : ILogPipeline {

    override fun addHook(hook: LogHook) = when (hook) {
        is LogPostHook -> postHook.add(hook)
        is LogPreHook -> preHook.add(hook)
    }.asUnit()

    override fun removeHook(hook: LogHook) = when (hook) {
        is LogPostHook -> postHook.remove(hook)
        is LogPreHook -> preHook.remove(hook)
    }.asUnit()

    override fun ILogMessageContext.process() {
        LogPipelineContext(this).apply {
            hook.run {
                call(hook {
                    preHook.pipeExecute(this, hook {
                        messageBuilder(this)
                        postHook.pipeExecute(this, output)
                    })
                })
            }
        }
    }

    override fun copyPipeline() = LogPipeline(messageBuilder, output,
        preHook.map { it?.copy() }.toMutableList(), postHook.map { it?.copy() }.toMutableList()
    )
}


fun <C : ICancellable> Collection<IDelayHook<C>?>.pipeExecute(
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
            if (isCancelled) return@hook
            h(this, hook innerHook@{
                if (isCancelled) return@innerHook
                lambda(this)
            })
        })
    }

    if (!context.isCancelled) (lambdas.lastOrNull() ?: endAction)(context)
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

inline fun <C : ICancellable> delayHook(crossinline block: C.(next: C.() -> Unit) -> Unit): IDelayHook<C> =
    object : IDelayHook<C> {
        override fun C.call(next: IHook<C>) = block { next(this) }
    }

inline fun <C : ICancellable> hook(crossinline block: C.() -> Unit): IHook<C> = object : IHook<C> {
    override fun C.call() = block()
}

sealed class LogHook : IDelayHook<LogPipelineContext> {
    abstract fun copy(): LogHook

    abstract class LogPreHook : LogHook() {
        abstract override fun copy(): LogPreHook
    }

    abstract class LogPostHook : LogHook() {
        abstract override fun copy(): LogPostHook
    }
}

inline fun logPreHook(crossinline block: LogPipelineContext.(next: LogPipelineContext.() -> Unit) -> Unit): LogPreHook =
    object : LogPreHook() {
        override fun LogPipelineContext.call(next: IHook<LogPipelineContext>) = block { next(this) }
        override fun copy(): LogPreHook = this
    }

inline fun logPostHook(crossinline block: LogPipelineContext.(next: LogPipelineContext.() -> Unit) -> Unit): LogPostHook =
    object : LogPostHook() {
        override fun LogPipelineContext.call(next: IHook<LogPipelineContext>) = block { next(this) }
        override fun copy(): LogPostHook = this
    }


interface IDelayHook<C : ICancellable> {
    fun C.call(next: IHook<C> = hook {})
    operator fun invoke(context: C, next: IHook<C> = hook {}) = context.run { call(next) }
}

interface IHook<in C : ICancellable> {
    fun C.call()
    operator fun invoke(context: C) = context.run { call() }
}

open class EmptyHook<C : ICancellable> : IDelayHook<C> {
    override fun C.call(next: IHook<C>) = next(this)
}

object EmptyPipelineLogHook : EmptyHook<LogPipelineContext>()