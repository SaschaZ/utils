package dev.zieger.utils.log.filter

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.LogPipelineContext
import dev.zieger.utils.time.ITimeSpan
import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.TimeStamp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

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