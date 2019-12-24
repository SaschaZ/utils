package de.gapps.utils.ta.output.chart

import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal


@Suppress("UNCHECKED_CAST")
interface IChartData : Map<IPlotConfig<IOhclVal, Any>, List<ITimeVal<Any?>>> {

    fun <PSD : IPlotConfig<IOhclVal, Any>, T> getSeries(
        indicatorConfig: IPlotConfig<IOhclVal, Any>
    ): Map<PSD, List<ITimeVal<T>>> = filterKeys { k ->
        k.seriesStyleType::class == indicatorConfig.seriesStyleType::class && k.plotLocation == indicatorConfig.plotLocation
    } as Map<PSD, List<ITimeVal<T>>>
}