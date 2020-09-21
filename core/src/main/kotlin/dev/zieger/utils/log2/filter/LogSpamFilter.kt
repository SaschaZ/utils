package dev.zieger.utils.log2.filter

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log2.IFilter
import dev.zieger.utils.log2.LogFilter
import dev.zieger.utils.log2.LogPipelineContext
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx
import dev.zieger.utils.time.minus
import dev.zieger.utils.time.plus
import dev.zieger.utils.time.times
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

data class LogSpamFilter(
    val duration: IDurationEx,
    private val scope: CoroutineScope,
    val tolerance: Float = 0.01f
) : LogFilter.LogPreFilter() {

    private var previousMessageAt: ITimeEx = TimeEx(0)
    private var lastMessageJob: Job? = null

    override fun LogPipelineContext.call(next: IFilter<LogPipelineContext>) {
        when {
            (TimeEx() - previousMessageAt) * (1 + tolerance) >= duration -> {
                lastMessageJob?.cancel()
                next(this)
                previousMessageAt = TimeEx()
            }
            else -> {
                lastMessageJob?.cancel()
                lastMessageJob = scope.launchEx(
                    delayed = previousMessageAt + duration * 2 - TimeEx(),
                    exclude = emptyList(),
                    onFinally = { lastMessageJob = null },
                    onCatch = { if (it is CancellationException) cancel() }
                ) {
                    next(this@call)
                    previousMessageAt = TimeEx()
                }
            }
        }
    }

    override fun copy() = LogSpamFilter(duration, scope, tolerance)
}