@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log

import dev.zieger.utils.log.ILogCache.LogMessage
import dev.zieger.utils.log.LogFilter.LogPostFilter
import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.misc.FiFo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ILogCache : IDelayFilter<ILogMessageContext> {

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

    override fun call(context: ILogMessageContext, next: IFilter<ILogMessageContext>) = context.run {
        scope.launch {
            mutex.withLock {
                cache[level.ordinal].second.put(LogMessage(message.toString(), context))
                listener(messages)
            }
        }
        next(this)
    }

    override fun reset() = cache.forEach { it.second.clear() }

    override fun copy() = LogCache(cacheLogLevel, cacheSize, scope, listener)
}