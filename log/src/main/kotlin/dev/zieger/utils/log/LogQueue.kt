@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log

import dev.zieger.utils.log.LogFilter.LogPostFilter
import dev.zieger.utils.log.LogFilter.LogPreFilter
import dev.zieger.utils.misc.asUnit
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import dev.zieger.utils.log.filter as logFilter

interface ILogQueue {
    var messageBuilder: ILogMessageBuilder
    var output: ILogOutput

    fun addFilter(filter: LogFilter)
    fun addPreFilter(
        block: ILogMessageContext.(next: ILogMessageContext.() -> Unit) -> Unit
    ) = addFilter(logPreFilter(block = block))

    fun addPostFilter(
        block: ILogMessageContext.(next: ILogMessageContext.() -> Unit) -> Unit
    ) = addFilter(logPostFilter(block = block))

    fun removeFilter(filter: LogFilter)

    fun ILogMessageContext.execute()

    fun copyQueue(): ILogQueue
}

/**
 *
 */
open class LogQueue(
    override var messageBuilder: ILogMessageBuilder,
    override var output: ILogOutput,
    val logTag: ILogTag,
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

    override fun ILogMessageContext.execute() {
        preHook.executeQueue(this, logFilter {
            if (isCancelled.get()) return@logFilter
            messageBuilder.call(this)
            postHook.executeQueue(this, output)
        })
    }

    override fun copyQueue() = LogQueue(messageBuilder, output, logTag,
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
        lambdas.add(logFilter {
            if (isCancelled.get()) return@logFilter
            f(this, logFilter innerFilter@{
                if (isCancelled.get()) return@innerFilter
                lambda(this)
            })
        })
    }

    if (!context.isCancelled.get()) (lambdas.lastOrNull() ?: endAction)(context)
}

interface ICancellable {

    val isCancelled: AtomicBoolean
    fun cancel()
}

interface IDelayFilter<C : ICancellable> {
    fun call(context: C, next: IFilter<C> = logFilter {})
    operator fun invoke(context: C, next: IFilter<C> = logFilter {}) = call(context, next)
}

interface IFilter<in C : ICancellable> {
    fun call(context: C)
    operator fun invoke(context: C) = call(context)
}

private inline fun <C : ICancellable> delayFilter(crossinline block: C.(next: C.() -> Unit) -> Unit): IDelayFilter<C> =
    object : IDelayFilter<C> {
        override fun call(context: C, next: IFilter<C>) = context.block { next(this) }
    }

private inline fun <C : ICancellable> filter(crossinline block: C.() -> Unit): IFilter<C> = object : IFilter<C> {
    override fun call(context: C) = block(context)
}

sealed class LogFilter : IDelayFilter<ILogMessageContext> {

    abstract fun copy(): LogFilter

    abstract class LogPreFilter() : LogFilter() {
        abstract override fun copy(): LogPreFilter
    }

    abstract class LogPostFilter() : LogFilter() {
        abstract override fun copy(): LogPostFilter
    }
}

inline fun logPreFilter(
    crossinline block: ILogMessageContext.(next: ILogMessageContext.() -> Unit) -> Unit
): LogPreFilter =
    object : LogPreFilter() {
        override fun call(context: ILogMessageContext, next: IFilter<ILogMessageContext>) = context.block { next(this) }
        override fun copy(): LogPreFilter = this
    }

inline fun logPostFilter(
    crossinline block: ILogMessageContext.(next: ILogMessageContext.() -> Unit) -> Unit
): LogPostFilter =
    object : LogPostFilter() {
        override fun call(context: ILogMessageContext, next: IFilter<ILogMessageContext>) = context.block { next(this) }
        override fun copy(): LogPostFilter = this
    }