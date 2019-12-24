@file:Suppress("LeakingThis")

package de.gapps.utils.ta.output.chart

import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal

interface ITypeChartData : IChartData {

    val main: MainChartData
    val top: TopChartData
    val bottom: BottomChartData
}

open class ChartData(values: Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>>) : ITypeChartData,
    Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>> by values {

    override val main = MainChartData(values)
    override val top = TopChartData(values)
    override val bottom = BottomChartData(values)
}

class MainChartData(values: Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>>) : IChartData,
    Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>> by values {

    fun getCandles(config: IPlotConfig<IOhclVal, Any>): Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<IOhclVal>>>? =
        getSeries(config)

    fun getCircledNumbers(config: IPlotConfig<IOhclVal, Any>): Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Int>>>? =
        getSeries(config)

    fun getSingleLines(config: IPlotConfig<IOhclVal, Any>): Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Double>>>? =
        getSeries(config)

    fun getTripleLines(config: IPlotConfig<IOhclVal, Any>): Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<STimeVal.TripleLineValue>>>? =
        getSeries(config)
}

class BottomChartData(values: Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>>) : IChartData,
    Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>> by values {

    fun getSingleLines(config: IPlotConfig<IOhclVal, Any>): Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Double>>>? =
        getSeries(config)
}

class TopChartData(values: Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>>) : IChartData,
    Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>> by values {

    fun getSingleLines(config: IPlotConfig<IOhclVal, Any>): Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Double>>>? =
        getSeries(config)
}