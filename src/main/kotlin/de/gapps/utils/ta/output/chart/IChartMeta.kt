package de.gapps.utils.ta.output.chart

import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.time.values.IOhclVal

interface IChartMeta {

    val metaForSeries: Map<IPlotConfig<IOhclVal, Any>, IPlotMeta>
    val metaForLocation: Map<EPlotLocation, IPlotMeta>
}