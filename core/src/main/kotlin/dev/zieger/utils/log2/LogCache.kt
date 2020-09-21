@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log2

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log2.ILogCache.LogMessage
import dev.zieger.utils.log2.LogFilter.LogPostFilter
import dev.zieger.utils.log2.filter.LogLevel
import dev.zieger.utils.misc.FiFo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

interface ILogCache : IDelayFilter<LogPipelineContext> {

    data class LogMessage(
        val msg: String,
        val context: ILogMessageContext
    )

    var cacheLogLevel: LogLevel
    val messages: List<LogMessage>

    fun reset()
}

class LogCache(
    override var cacheLogLevel: LogLevel = LogLevel.VERBOSE,
    private val cacheSize: Int = DEFAULT_CACHE_SIZE,
    private val scope: CoroutineScope,
    private val listener: suspend ILogCache.(messages: List<LogMessage>) -> Unit = {}
) : LogPostFilter(), ILogCache {

    companion object {

        const val DEFAULT_CACHE_SIZE = 1024
    }

    private val mutex = Mutex()
    private val cache = Array<Pair<LogLevel, FiFo<LogMessage>>>(LogLevel.values().size) {
        LogLevel.values()[it] to FiFo(cacheSize)
    }
    override val messages
        get() = cache.filter { it.first >= cacheLogLevel }.flatMap { it.second }
            .sortedBy { it.context.createdAt }

    init {
        reset()
    }

    override fun LogPipelineContext.call(next: IFilter<LogPipelineContext>) {
        scope.launchEx(mutex = mutex) {
            cache[level.ordinal].second.put(LogMessage(message.toString(), this@call))
            listener(messages)
        }
        next(this)
    }

    override fun reset() = cache.forEach { it.second.clear() }

    override fun copy() = LogCache(cacheLogLevel, cacheSize, scope, listener)
}