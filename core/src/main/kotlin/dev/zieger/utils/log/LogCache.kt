package dev.zieger.utils.log

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.FiFo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

interface ILogCache : ILogOutput {

    data class LogMessage(
        val msg: String,
        val context: ILogMessageContext
    )

    fun getCached(minLevel: LogLevel = LogLevel.VERBOSE): List<LogMessage>
}

class CachingOutput(
    private val printOutput: ILogOutput = SystemPrintOutput,
    private val scope: CoroutineScope = DefaultCoroutineScope()
) : ILogCache {

    companion object {

        const val CACHE_SIZE = 1024
    }

    private val cache = HashMap<LogLevel, FiFo<ILogCache.LogMessage>>()
    private val cacheInput = Channel<ILogCache.LogMessage>(CACHE_SIZE * 2)

    init {
        LogLevel.values().forEach { logLevel -> cache[logLevel] = FiFo(CACHE_SIZE) }

        scope.launchEx {
            for (logMessage in cacheInput)
                cache[logMessage.context.level]!!.put(logMessage)
        }
    }

    override fun ILogMessageContext.write(msg: String) {
        printOutput.run { write(msg) }

        val message = ILogCache.LogMessage(msg, this)
        if (!cacheInput.offer(message))
            scope.launchEx { cacheInput.send(message) }
    }

    override fun getCached(minLevel: LogLevel): List<ILogCache.LogMessage> =
        cache.entries.filter { it.key >= minLevel }.flatMap { it.value }.sortedBy { it.context.createdAt }
}