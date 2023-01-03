@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log

import dev.zieger.utils.log.LogFilter.LogPostFilter
import dev.zieger.utils.log.LogFilter.LogPreFilter
import dev.zieger.utils.misc.asUnit
import java.util.*

interface ILogQueue {
    var messageBuilder: ILogMessageBuilder
    var output: ILogOutput

    fun addFilter(filter: LogFilter)
    fun addPreFilter(
        tag: Any? = null,
        block: ILogQueueContext.(next: ILogQueueContext.() -> Unit) -> Unit
    ) = addFilter(logPreFilter(tag, block))

    fun addPostFilter(
        tag: Any? = null,
        block: ILogQueueContext.(next: ILogQueueContext.() -> Unit) -> Unit
    ) = addFilter(logPostFilter(tag, block))

    fun removeFilter(filter: LogFilter)

    fun ILogMessageContext.process()

    fun copyQueue(): ILogQueue
}

/**
 *
 */
open class LogQueue(
    override var messageBuilder: ILogMessageBuilder,
    override var output: ILogOutput,
    private val preHook: MutableList<LogPreFilter?> = ArrayList(),
    private val postHook: MutableList<LogPostFilter?> = ArrayList()
) : ILogQueue {

    override fun addFilter(filter: LogFilter) = when (filter) {
        is LogPostFilter -> postHook.add(filter)
        is LogPreFilter -> preHook.add(filter)
    }.asUnit()

    override fun removeFilter(filter: LogFilter) = when (filter) {
        is LogPostFilter -> postHook.remove(filter)
        is LogPreFilter -> preHook.remove(filter)
    }.asUnit()

    override fun ILogMessageContext.process() {
        LogQueueContext(this).apply {
            filter.run {
                call(this@apply, filter {
                    preHook.executeQueue(this, filter {
                        messageBuilder(this)
                        postHook.executeQueue(this, output)
                    })
                })
            }
        }
    }

    override fun copyQueue() = LogQueue(messageBuilder, output,
        preHook.map { it?.copy() }.toMutableList(), postHook.map { it?.copy() }.toMutableList()
    )
}


private fun <C : ICancellable> Collection<IDelayFilter<C>?>.executeQueue(
    context: C,
    endAction: IFilter<C>
) {
    val lambdas = LinkedList<IFilter<C>>()
    for ((idx, f) in ArrayList(toList().filterNotNull()).reversed().withIndex()) {
        val lambda = when (idx) {
            0 -> endAction
            else -> lambdas[idx - 1]
        }
        lambdas.add(dev.zieger.utils.log.filter {
            if (isCancelled) return@filter
            f(this, dev.zieger.utils.log.filter innerFilter@{
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
    fun call(context: C, next: IFilter<C> = filter {})
    operator fun invoke(context: C, next: IFilter<C> = filter {}) = call(context, next)
}

interface IFilter<in C : ICancellable> {
    fun call(context: C)
    operator fun invoke(context: C) = call(context)
}

interface ILogQueueContext : ILogMessageContext, ICancellable

open class LogQueueContext(
    messageContext: ILogMessageContext
) : ILogQueueContext, ILogMessageContext by messageContext {

    override var isCancelled: Boolean = false

    override fun cancel() {
        isCancelled = true
    }
}

private inline fun <C : ICancellable> delayFilter(crossinline block: C.(next: C.() -> Unit) -> Unit): IDelayFilter<C> =
    object : IDelayFilter<C> {
        override fun call(context: C, next: IFilter<C>) = context.block { next(this) }
    }

private inline fun <C : ICancellable> filter(crossinline block: C.() -> Unit): IFilter<C> = object : IFilter<C> {
    override fun call(context: C) = block(context)
}

sealed class LogFilter : IDelayFilter<ILogQueueContext> {

    abstract val tag: Any?
    abstract fun copy(): LogFilter

    abstract class LogPreFilter(override val tag: Any? = null) : LogFilter() {
        abstract override fun copy(): LogPreFilter
    }

    abstract class LogPostFilter(override val tag: Any? = null) : LogFilter() {
        abstract override fun copy(): LogPostFilter
    }
}

inline fun logPreFilter(
    tag: Any? = null,
    crossinline block: ILogQueueContext.(next: ILogQueueContext.() -> Unit) -> Unit
): LogPreFilter =
    object : LogPreFilter(tag) {
        override fun call(context: ILogQueueContext, next: IFilter<ILogQueueContext>) = context.block { next(this) }
        override fun copy(): LogPreFilter = this
    }

inline fun logPostFilter(
    tag: Any? = null,
    crossinline block: ILogQueueContext.(next: ILogQueueContext.() -> Unit) -> Unit
): LogPostFilter =
    object : LogPostFilter(tag) {
        override fun call(context: ILogQueueContext, next: IFilter<ILogQueueContext>) = context.block { next(this) }
        override fun copy(): LogPostFilter = this
    }

object EmptyLogFilter : LogPreFilter(), IDelayFilter<ILogQueueContext> {
    override fun call(context: ILogQueueContext, next: IFilter<ILogQueueContext>) = next(context)
    override fun copy(): EmptyLogFilter = this
}