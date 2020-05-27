@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.ILogCache.LogMessage
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.time.ITimeEx
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
    private val scope: CoroutineScope = DefaultCoroutineScope(),
    private val listener: ILogCache.(lvl: LogLevel, msg: String, time: ITimeEx) -> Unit = { _, _, _ -> }
) : ILogCache {

    companion object {

        const val CACHE_SIZE = 1024
    }

    private val cache = HashMap<LogLevel, FiFo<LogMessage>>()
    private val cacheInput = Channel<LogMessage>(CACHE_SIZE * 2)

    init {
        LogLevel.values().forEach { logLevel -> cache[logLevel] = FiFo(CACHE_SIZE) }

        scope.launchEx {
            for (logMessage in cacheInput) logMessage.run {
                cache[context.level]!!.put(logMessage)
                listener(context.level, msg, context.createdAt)
            }
        }
    }

    override fun ILogMessageContext.write(msg: String) {
        printOutput.run { write(msg) }

        val message = LogMessage(msg, this)
        if (!cacheInput.offer(message))
            scope.launchEx { cacheInput.send(message) }
    }

    override fun getCached(minLevel: LogLevel): List<LogMessage> =
        cache.entries.filter { it.key >= minLevel }.flatMap { it.value }.sortedBy { it.context.createdAt }
}