package de.gapps.utils.ta.input.config

import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.ta.processing.SSeriesStyleType
import de.gapps.utils.time.time
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal

data class RsiConfig<I : IOhclVal>(
    val length: Int,
    val triggerLength: Int,
    override val plotLocation: EPlotLocation = EPlotLocation.TOP,
    override val seriesStyleType: SSeriesStyleType = SSeriesStyleType.SINGLE_LINE(),
    override val idx: Int = 0,
    override val valueSelector: (ITimeVal<I>) -> ITimeVal<Double> = {
        STimeVal.TimeVal(
            it.value.close,
            it.time
        )
    }
) : IPlotConfig<I, Double>