package dev.zieger.utils.log.filter

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.ILogContext
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.LogPipelineContext
import dev.zieger.utils.time.ITimeSpan
import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.TimeStamp
import dev.zieger.utils.time.millis
import kotlinx.coroutines.*

data class LogSpamFilter(
    val minInterval: ITimeSpan,
    private val scope: CoroutineScope,
    val tolerance: Float = 0.01f
) : LogFilter.LogPreFilter() {

    private var previousMessageAt: ITimeStamp = TimeStamp(0.0)
    private var lastMessageJob: Job? = null

    override fun call(context: LogPipelineContext, next: IFilter<LogPipelineContext>) = context.run {
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
        ((messageTag ?: tag) as? LogId)?.id?.let { id ->
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
    val ctx: LogPipelineContext,
    val next: LogPipelineContext.() -> Unit
)

interface LogId {
    var id: Any?
}

infix fun <T : LogId> T.withId(id: Any): T {
    this.id = id
    return this
}