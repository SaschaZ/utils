package de.gapps.utils.ta.output.chart.styles

import de.gapps.utils.ta.input.IChartRequest

interface ISeriesStyle<CR : IChartRequest> {

    fun CR.init(): List<String> = emptyList()
    fun CR.item(indices: Pair<Int, Int>): List<String> = emptyList()
}