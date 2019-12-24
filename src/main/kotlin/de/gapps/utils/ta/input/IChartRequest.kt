package de.gapps.utils.ta.input

import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.ta.input.currency.ICandleParameter
import de.gapps.utils.ta.processing.timeval.ITimeValProcessor
import de.gapps.utils.time.values.IOhclVal


interface IChartRequest {

    val candleParameter: ICandleParameter
    var filenames: Filenames
    val plots: List<IndicatorIdentifier<IOhclVal, Any, IPlotConfig<IOhclVal, Any>, Any, ITimeValProcessor<IOhclVal, Any>>>
}

data class Filenames(val filenameBase: String) {

    val data = "$filenameBase.dat"
    val script = "$filenameBase.gnuplot"
    val svg = "$filenameBase.svg"
}

operator fun Filenames.plus(suffix: String) = Filenames(filenameBase + suffix)