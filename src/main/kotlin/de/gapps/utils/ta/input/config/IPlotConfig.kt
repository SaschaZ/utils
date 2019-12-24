package de.gapps.utils.ta.input.config

import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.ta.processing.SSeriesStyleType
import de.gapps.utils.time.time
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal

interface IPlotConfig<I : IOhclVal, out O : Any> {

    val seriesStyleType: SSeriesStyleType
    val plotLocation: EPlotLocation
    val idx: Int

    @Suppress("UNCHECKED_CAST")
    val valueSelector: (ITimeVal<I>) -> ITimeVal<O>
        get() = { STimeVal.TimeVal(it.value as O, it.time) }
}