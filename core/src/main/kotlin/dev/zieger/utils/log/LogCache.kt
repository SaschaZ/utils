@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.ILogCache.LogMessage
import dev.zieger.utils.misc.FiFo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

interface ILogCache : ILogPreHook {

    data class LogMessage(
        val msg: String,
        val context: ILogMessageContext
    )

    var cacheLogLevel: LogLevel
    val messages: List<LogMessage>

    fun reset()
}

class LogCache(
    private val scope: CoroutineScope = DefaultCoroutineScope(),
    override var cacheLogLevel: LogLevel = LogLevel.VERBOSE,
    private val listener: suspend ILogCache.(messages: List<LogMessage>) -> Unit = {}
) : ILogCache {

    companion object {

        const val CACHE_SIZE = 1024
    }

    private val mutex = Mutex()
    private val cache = HashMap<LogLevel, FiFo<LogMessage>>()
    override val messages
        get() = cache.entries.filter { it.key >= cacheLogLevel }.flatMap { it.value }
            .sortedBy { it.context.createdAt }

    init {
        reset()
    }

    override val onPreHook: ILogMessageContext.(String) -> Unit = { msg ->
        val ctx = this
        scope.launchEx(mutex = mutex) {
            val message = LogMessage(msg, ctx)
            cache[message.context.level]?.put(message)

            listener(messages)
        }
    }

    override fun reset() {
        cache.clear()
        LogLevel.values().forEach { logLevel -> cache[logLevel] = FiFo(CACHE_SIZE) }
    }
}