@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log2

import dev.zieger.utils.log2.LogFilter.LogPostFilter
import dev.zieger.utils.log2.LogFilter.LogPreFilter
import dev.zieger.utils.misc.asUnit
import java.util.*
import kotlin.collections.ArrayList

interface ILogPipeline {
    var messageBuilder: ILogMessageBuilder
    var output: ILogOutput

    fun addHook(filter: LogFilter)
    fun removeHook(filter: LogFilter)

    operator fun plusAssign(filter: LogFilter) = addHook(filter)
    operator fun minusAssign(filter: LogFilter) = removeHook(filter)

    fun ILogMessageContext.process()

    fun copyPipeline(): ILogPipeline
}

/**
 *
 */
open class LogPipeline(
    override var messageBuilder: ILogMessageBuilder,
    override var output: IFilter<LogPipelineContext>,
    private val preHook: MutableList<LogPreFilter?> = ArrayList(),
    private val postHook: MutableList<LogPostFilter?> = ArrayList()
) : ILogPipeline {

    override fun addHook(filter: LogFilter) = when (filter) {
        is LogPostFilter -> postHook.add(filter)
        is LogPreFilter -> preHook.add(filter)
    }.asUnit()

    override fun removeHook(filter: LogFilter) = when (filter) {
        is LogPostFilter -> postHook.remove(filter)
        is LogPreFilter -> preHook.remove(filter)
    }.asUnit()

    override fun ILogMessageContext.process() {
        LogPipelineContext(this).apply {
            filter.run {
                call(filter {
                    preHook.pipeExecute(this, filter {
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


fun <C : ICancellable> Collection<IDelayFilter<C>?>.pipeExecute(
    context: C,
    endAction: IFilter<C>
) {
    val lambdas = LinkedList<IFilter<C>>()
    for ((idx, f) in ArrayList(toList().filterNotNull()).reversed().withIndex()) {
        val lambda = when (idx) {
            0 -> endAction
            else -> lambdas[idx - 1]
        }
        lambdas.add(dev.zieger.utils.log2.filter {
            if (isCancelled) return@filter
            f(this, dev.zieger.utils.log2.filter innerFilter@{
                if (isCancelled) return@innerFilter
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

interface IDelayFilter<C : ICancellable> {
    fun C.call(next: IFilter<C> = filter {})
    operator fun invoke(context: C, next: IFilter<C> = filter {}) = context.run { call(next) }
}

interface IFilter<in C : ICancellable> {
    fun C.call()
    operator fun invoke(context: C) = context.run { call() }
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

inline fun <C : ICancellable> delayFilter(crossinline block: C.(next: C.() -> Unit) -> Unit): IDelayFilter<C> =
    object : IDelayFilter<C> {
        override fun C.call(next: IFilter<C>) = block { next(this) }
    }

inline fun <C : ICancellable> filter(crossinline block: C.() -> Unit): IFilter<C> = object : IFilter<C> {
    override fun C.call() = block()
}

sealed class LogFilter : IDelayFilter<LogPipelineContext> {
    abstract fun copy(): LogFilter

    abstract class LogPreFilter : LogFilter() {
        abstract override fun copy(): LogPreFilter
    }

    abstract class LogPostFilter : LogFilter() {
        abstract override fun copy(): LogPostFilter
    }
}

inline fun logPreFilter(crossinline block: LogPipelineContext.(next: LogPipelineContext.() -> Unit) -> Unit): LogPreFilter =
    object : LogPreFilter() {
        override fun LogPipelineContext.call(next: IFilter<LogPipelineContext>) = block { next(this) }
        override fun copy(): LogPreFilter = this
    }

inline fun logPostFilter(crossinline block: LogPipelineContext.(next: LogPipelineContext.() -> Unit) -> Unit): LogPostFilter =
    object : LogPostFilter() {
        override fun LogPipelineContext.call(next: IFilter<LogPipelineContext>) = block { next(this) }
        override fun copy(): LogPostFilter = this
    }


open class EmptyFilter : LogPreFilter(), IDelayFilter<LogPipelineContext> {
    override fun LogPipelineContext.call(next: IFilter<LogPipelineContext>) = next(this)
    override fun copy(): LogPreFilter = EmptyFilter()
}

object EmptyLogFilter : EmptyFilter()