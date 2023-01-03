package dev.zieger.utils.log.filter

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.ILogContext
import dev.zieger.utils.log.ILogQueueContext
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.time.*
import kotlinx.coroutines.*
import java.util.*


fun ILogContext.addMessageSpamFilter(
    spamDuration: ITimeSpan = 1.seconds
) {
    val scope = CoroutineScope(Dispatchers.IO)
    val filterItems = HashMap<LogMessage, LinkedList<ITimeStamp>>()
    val nextJobs = HashMap<LogMessage, Pair<Job, () -> Unit>>()

    addSpamFilter { msg ->
        val times = filterItems.getOrPut(msg) { LinkedList() }
        val now = TimeStamp()
        times.add(now)
        val messagesPerSecond = times.count { now - it < spamDuration }

        if (messagesPerSecond <= 1)
            0.seconds
        else spamDuration * 1.5
    }
}

fun ILogContext.addSpamFilter(block: ILogQueueContext.(LogMessage) -> ITimeSpan?) {
    val scope = CoroutineScope(Dispatchers.IO)
    val delayed = HashMap<LogMessage, () -> Unit>()
    val filterCount = HashMap<LogMessage, Int>()

    addPreFilter { next ->
        val msg = LogMessage(level, messageTag ?: tag, message, createdAt)
        when (val toDelay = block(msg)) {
            null -> {
                filterCount[msg] = 0
                cancel()
            }

            0.millis -> {
                filterCount[msg] = 0
                next()
            }

            else -> {
                delayed[msg]?.invoke()
                filterCount[msg] = (filterCount[msg] ?: 0) + 1
                val job = scope.launch {
                    delay(toDelay)
                    delayed.remove(msg)
                    message = "$message (was filtered ${filterCount[msg]} times)"
                    filterCount[msg] = 0
                    next()
                }
                delayed[msg] = {
                    job.cancel()
                    cancel()
                }
            }
        }
    }
}

class LogMessage internal constructor(
    val level: LogLevel,
    val tag: Any?,
    val message: Any,
    val createdAt: ITimeStamp
) {
    override fun equals(other: Any?): Boolean = (other as? LogMessage)?.let { o ->
        level == o.level
                && tag == o.tag
                && message == o.message
    } == true

    override fun hashCode(): Int = level.hashCode() + tag.hashCode() + message.hashCode()
}

data class LogSpamFilter(
    val minInterval: ITimeSpan,
    private val scope: CoroutineScope,
    val tolerance: Float = 0.01f
) : LogFilter.LogPreFilter() {

    private var previousMessageAt: ITimeStamp = TimeStamp(0.0)
    private var lastMessageJob: Job? = null

    override fun call(context: ILogQueueContext, next: IFilter<ILogQueueContext>) = context.run {
        val diff = TimeStamp() - previousMessageAt
        lastMessageJob?.cancel()
        when {
            diff * (1 - tolerance) >= minInterval -> {
                next(context)
                previousMessageAt = TimeStamp()
            }

            else -> {
                lastMessageJob = scope.launchEx(
                    delayed = minInterval - diff,
                    printStackTrace = false,
                    exclude = emptyList(),
                    onFinally = { lastMessageJob = null },
                    onCatch = { if (it is CancellationException) cancel() }
                ) {
                    next(context)
                    previousMessageAt = TimeStamp()
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
    val ctx: ILogQueueContext,
    val next: ILogQueueContext.() -> Unit
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

val Any.id: Any?
    get() = (this as? ILogId)?.id

val Any.tag: Any
    get() = (this as? ILogId)?.tag ?: this