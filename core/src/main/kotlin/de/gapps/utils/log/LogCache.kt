package de.gapps.utils.log

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.FiFo
import de.gapps.utils.misc.name
import de.gapps.utils.time.duration.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.ReceiveChannel

class LogCache private constructor(private val scope: CoroutineScopeEx = DefaultCoroutineScope(LogCache::class.name)) :
    LogElement {

    companion object {

        fun initialize() = LogCache().apply {
            Log + this
        }

        private const val INPUT_CAPACITY = 1024
        private const val FIFO_CAPACITY = 1024
    }

    private var outputJob: Job? = null
    private var outputCancelCount = 0

    private val fifo = FiFo<Triple<LogLevel?, String, Long>>(FIFO_CAPACITY)
    private val input = Channel<Triple<LogLevel?, String, Long>>(INPUT_CAPACITY)
    private val output = Channel<List<Triple<LogLevel?, String, Long>>>(CONFLATED)
    val out = output as ReceiveChannel<List<Triple<LogLevel?, String, Long>>>

    init {
        scope.launchEx {
            for (triple in input) {
                fifo.put(triple)
                logOutputInvalidated()
            }
        }
    }

    private fun logOutputInvalidated() {
        if (outputCancelCount == 10) {
            outputJob?.cancel()
            outputCancelCount = 0
            scope.launchEx { output.send(ArrayList(fifo)) }
        } else {
            outputJob?.cancel()
            outputCancelCount++
            outputJob = scope.launchEx(delayed = 100.milliseconds) {
                outputCancelCount = 0
                output.send(ArrayList(fifo))
            }
        }
    }

    override fun log(level: LogLevel?, msg: String): Boolean {
        val logMessage = level to msg to System.nanoTime()
        if (!input.offer(logMessage)) scope.launchEx { input.send(logMessage) }
        return true
    }
}

infix fun <A, B, C> Pair<A, B>.to(c: C) = Triple(first, second, c)