package de.gapps.utils.ta.input

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.MainCoroutineScope
import de.gapps.utils.misc.FiFo
import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.ta.input.config.RawValueConfig
import de.gapps.utils.ta.output.chart.ChartData
import de.gapps.utils.time.time
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlotDataCollector(private val request: CandleSticks) {

    private val scope = MainCoroutineScope()

    private val values = HashMap<IPlotConfig<IOhclVal, Any>, FiFo<ITimeVal<Any?>>>()
    val isFull: Boolean
        get() = values.any { it.value.isFull }

    private val dataMutex = Mutex()
    suspend fun plotData() = dataMutex.withLock { ChartData(values) }

    @Suppress("UNCHECKED_CAST")
    val candles: List<ITimeVal<IOhclVal>>
        get() = values[RawValueConfig<IOhclVal, Double>()]?.map { it as ITimeVal<IOhclVal> } ?: emptyList()

    fun putValues(newValues: Map<IPlotConfig<IOhclVal, Any>, ITimeVal<Any?>>) = scope.launchEx(mutex = dataMutex) {
        if (newValues.isNotEmpty()) newValues.forEach { putValue(it.toPair()) }
    }

    private fun putValue(value: Pair<IPlotConfig<IOhclVal, Any>, ITimeVal<Any?>>) {
        values.getOrPut(value.first) { FiFo(request.amount) }.let { fifo ->
            fifo.put(value.second, value.second.time == fifo.lastOrNull()?.time)
        }
    }
}