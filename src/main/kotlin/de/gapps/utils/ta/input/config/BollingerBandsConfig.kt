@file:Suppress("UNCHECKED_CAST")

package de.gapps.utils.ta.input.config

import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.ta.processing.SSeriesStyleType
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal

data class BollingerBandsConfig<I : IOhclVal>(
    val length: Int = 20,
    val stdDevFactor: Double = 2.0,
    val avgConfig: IPlotConfig<IOhclVal, Double> = MaConfig(length),
    override val plotLocation: EPlotLocation = EPlotLocation.MAIN,
    override val seriesStyleType: SSeriesStyleType = SSeriesStyleType.TRIPLE_LINE(),
    override val idx: Int = 0,
    override val valueSelector: (ITimeVal<I>) -> ITimeVal<I> = { it }
) : IPlotConfig<I, I>