package de.gapps.utils.ta.input.config

import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.ta.processing.SSeriesStyleType
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal

data class ParabolicStarConfig<I : IOhclVal>(
    val maxAbsoluteExtremeCount: Int = 10,
    val extremeCandleRange: Int = 20,
    override val plotLocation: EPlotLocation = EPlotLocation.MAIN,
    override val seriesStyleType: SSeriesStyleType = SSeriesStyleType.SINGLE_LINE(),
    override val idx: Int = 0,
    override val valueSelector: (ITimeVal<I>) -> ITimeVal<I>
) : IPlotConfig<I, I>