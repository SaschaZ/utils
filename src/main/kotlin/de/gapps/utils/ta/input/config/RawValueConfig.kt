package de.gapps.utils.ta.input.config

import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.ta.processing.SSeriesStyleType
import de.gapps.utils.time.time
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal

data class RawValueConfig<I : IOhclVal, out T : Any>(
    override val seriesStyleType: SSeriesStyleType = SSeriesStyleType.CANDLE(),
    override val plotLocation: EPlotLocation = EPlotLocation.MAIN,
    override val idx: Int = 0,
    override val valueSelector: (ITimeVal<I>) -> ITimeVal<T> = {
        @Suppress("UNCHECKED_CAST")
        STimeVal.TimeVal(it.value as T, it.time)
    }
) : IPlotConfig<I, T>



