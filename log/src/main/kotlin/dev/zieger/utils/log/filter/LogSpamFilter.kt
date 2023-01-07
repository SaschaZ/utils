package dev.zieger.utils.log.filter

import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.ILogContext
import dev.zieger.utils.log.ILogMessageContext
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.time.*
import kotlinx.coroutines.*
import java.util.*


fun ILogContext.addMessageSpamFilter(
    spamDuration: ITimeSpan = 1.seconds
) {
    val scope = CoroutineScope(Dispatchers.IO)
    val filterItems = HashMap<Any, LinkedList<ITimeStamp>>()
    val nextJobs = HashMap<Any, Pair<Job, () -> Unit>>()

    addSpamFilter {
        val times = filterItems.getOrPut(message) { LinkedList() }
        val now = TimeStamp()
        times.add(now)
        val messagesPerSecond = times.count { now - it < spamDuration }

        "$message" to if (messagesPerSecond <= 1)
            0.seconds
        else spamDuration
    }
}

fun ILogContext.addSpamFilter(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    block: ILogMessageContext.(String) -> Pair<Any, ITimeSpan?>
) {
    val delayed = HashMap<Any, () -> Unit>()
    val filterCount = HashMap<Any, Int>()

    addPostFilter { next ->
        val (key, toDelay) = block("$message")
        when (toDelay) {
            null -> {
                filterCount[key] = 0
                cancel()
            }

            0.millis -> {
                filterCount[key] = 0
                next()
            }

            else -> {
                delayed[key]?.invoke()
                filterCount[key] = (filterCount[key] ?: 0) + 1
                val job = scope.launch {
                    delay(toDelay)
                    delayed.remove(key)
                    builtMessage = "$builtMessage (was filtered ${filterCount[key]} times)"
                    filterCount[key] = 0
                    next()
                }
                delayed[key] = {
                    job.cancel()
                    cancel()
                }
            }
        }
    }
}

data class LogSpamFilter(
    val minInterval: ITimeSpan,
    private val scope: CoroutineScope,
    val tolerance: Float = 0.01f
) : LogFilter.LogPreFilter() {

    private var previousMessageAt: ITimeStamp = TimeStamp(0.0)
    private var lastMessageJob: Job? = null

    override fun call(context: ILogMessageContext, next: IFilter<ILogMessageContext>) = context.run {
        val diff = TimeStamp() - previousMessageAt
        lastMessageJob?.cancel()
        when {
            diff * (1 - tolerance) >= minInterval -> {
                next(context)
                previousMessageAt = TimeStamp()
            }

            else -> {
                lastMessageJob = scope.launch {
                    next(context)
                    previousMessageAt = TimeStamp()
                    lastMessageJob = null
                }
            }
        }
    }

    override fun copy() = LogSpamFilter(minInterval, scope, tolerance)
}


fun ILogContext.spamFilter(
    scope: CoroutineScope,
    interval: ITimeSpan = 500.millis
) {
    val spamMessages = HashMap<Any, SpammyLogMessage>()

    addPreFilter { next ->
        ((messageTag ?: tag) as? ILogId)?.id?.let { id ->
            spamMessages[id]?.let {
                if (it.job.isActive) {
                    it.job.cancel()
                    if (it.message.toString() != message.toString())
                        it.next(it.ctx)
                }
            }
            spamMessages[id] = SpammyLogMessage(
                scope.launch {
                    delay(interval.millisLong)
                    if (isActive) next()
                },
                message,
                this, next
            )
        } ?: next()
    }
}

private data class SpammyLogMessage(
    val job: Job,
    val message: Any,
    val ctx: ILogMessageContext,
    val next: ILogMessageContext.() -> Unit
)

interface ILogId {
    val id: Any?
}

class LogTagId(
    val tag: Any?,
    override val id: Any?
) : ILogId {
    override fun toString(): String = "$tag"
}

infix fun Any.withId(id: Any): Any = LogTagId(this, id)