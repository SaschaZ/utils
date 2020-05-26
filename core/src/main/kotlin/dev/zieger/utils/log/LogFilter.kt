package dev.zieger.utils.log

import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.lastOrNull
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds

/**
 * Log-Filter
 */
interface ILogFilters {
    val filters: MutableList<ILogFilter>

    operator fun plus(filter: ILogFilter): ILogFilters = apply { filters.add(filter) }

    fun ILogFilters.copy(filters: List<ILogFilter> = this.filters): ILogFilters
}

class LogFilters(override val filters: MutableList<ILogFilter> = ArrayList()) :
    ILogFilters {
    constructor(filter: ILogFilter) : this(mutableListOf(filter))

    override fun ILogFilters.copy(filters: List<ILogFilter>): ILogFilters = LogFilters(filters.toMutableList())
}

operator fun ILogFilter.unaryPlus(): ILogFilters =
    LogFilters(this)

interface ILogFilter {
    fun ILogMessageContext.filter(action: () -> Unit)
}

object LogLevelFilter : ILogFilter {
    override fun ILogMessageContext.filter(action: () -> Unit) =
        if (minLogLevel <= level) action() else Unit
}

interface IResetScope {
    fun reset()
}

data class SpamFilter(
    val minInterval: IDurationEx = 1.milliseconds,
    val id: String,
    val sameMessage: Boolean = false,
    val resetScope: IResetScope.() -> Unit = {}
) : ILogFilter, IResetScope {

    companion object {
        private val idTimeMap = HashMap<String, ITimeEx>()
        private val idMessageMap = HashMap<String, FiFo<String>>()

        private const val MESSAGE_FIFO_SIZE = 128
    }

    override fun ILogMessageContext.filter(action: () -> Unit) {
        resetScope()

        idMessageMap.getOrPut(id) { FiFo(MESSAGE_FIFO_SIZE) }.also { fifo ->
            if (fifo.lastOrNull() == message) {
                if (sameMessage) return
            } else fifo.put(message)
        }

        when {
            idTimeMap[id]?.let { createdAt - it >= minInterval } != false -> {
                idTimeMap[id] = createdAt
                action()
            }
            else -> Unit
        }
    }

    override fun reset() = idTimeMap.remove(id).asUnit()
}

class ExternalFilter(val block: () -> Boolean) : ILogFilter {
    constructor(fixed: Boolean) : this({ fixed })

    override fun ILogMessageContext.filter(action: () -> Unit) =
        if (!block()) action() else Unit
}