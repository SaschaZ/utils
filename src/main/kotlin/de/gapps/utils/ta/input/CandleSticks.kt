package de.gapps.utils.ta.input

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.log.Log
import de.gapps.utils.misc.Vec2d
import de.gapps.utils.misc.whenNotNull
import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.ta.input.currency.CandleParameterExternal
import de.gapps.utils.ta.output.chart.*
import de.gapps.utils.ta.processing.timeval.ITimeValProcessor
import de.gapps.utils.time.time
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.ReceiveChannel

data class CandleSticks(
    override val candleParameter: CandleParameterExternal,
    val amount: Int = 300,
    val invisibleStartOffset: Int = 150,
    val visibleCandleCount: Int = amount - invisibleStartOffset,
    val visibleSideOffsets: Pair<Int, Int> = 1 to 2,
    val sizeFactor: Vec2d = Vec2d(1, 1),
    val lineWidth: Int = sizeFactor.min.toInt(),
    val xTicsMinutes: Int = 5,
    override var filenames: Filenames = Filenames("${candleParameter.pair.raw()}_${candleParameter.intervalPretty}"),
    override val plots: List<IndicatorIdentifier<IOhclVal, Any, IPlotConfig<IOhclVal, Any>, Any, ITimeValProcessor<IOhclVal, Any>>> = emptyList()
) : IChartRequest, IProcessor<Map<IPlotConfig<IOhclVal, Any>, ITimeVal<Any?>>, ITimeVal<SingleChart>> {

    private val valFiFo = PlotDataCollector(this)

    override fun ReceiveChannel<Map<IPlotConfig<IOhclVal, Any>, ITimeVal<Any?>>>.process() =
        Channel<ITimeVal<SingleChart>>(CONFLATED).also { output ->
            Log.d("buildModel()")
            val input = this
            DefaultCoroutineScope().apply {
                launchEx {
                    for (indicatorVal in input) {
                        valFiFo.putValues(indicatorVal)
                        if (valFiFo.isFull)
                            whenNotNull(
                                processPlotData(valFiFo.plotData()),
                                valFiFo.candles.lastOrNull()?.time
                            ) { d, t -> output.send(STimeVal.TimeVal(d, t)) }
                    }
                }
            }
        }

    private fun processPlotData(chartData: ChartData) = SingleChart(
        this,
        ChartInput(chartData),
        sizeFactor,
        ChartTitle(
            candleParameter.stringPretty,
            24.0 * sizeFactor.min,
            0xFFFFFFFF,
            "Verdana"
        ),
        16.0 * sizeFactor.min,
        ChartMargin(0.0, 14.0, 10.0),
        lineWidth,
        visibleCandleCount,
        2 to 2,
        xTicsMinutes
    )
}