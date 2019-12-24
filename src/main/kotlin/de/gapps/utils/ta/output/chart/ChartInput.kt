package de.gapps.utils.ta.output.chart

import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.time.values.IOhclVal

class ChartInput(
    override val data: ITypeChartData
) : IChartInput {

    override val meta = object : IChartMeta {

        override val metaForSeries: Map<IPlotConfig<IOhclVal, Any>, IPlotMeta> =
            data.map { it.key to PlotMeta(it.value) }.toMap()

        override val metaForLocation: Map<EPlotLocation, IPlotMeta> =
            EPlotLocation.values().map { type ->
                type to PlotMeta(data.entries.filter { it.key.plotLocation == type }
                    .flatMap { it.value })
            }.toMap()
    }
}