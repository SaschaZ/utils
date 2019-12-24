package de.gapps.utils.ta.output.chart.styles

import de.gapps.ctt.plotter.meta.output.color.PlotColor
import de.gapps.ctt.plotter.meta.output.color.SimpleColors
import de.gapps.utils.ta.input.CandleSticks


sealed class SeriesStyle : ISeriesStyle<CandleSticks> {

    class Ohcl(
        private val color: PlotColor.Palette<*> = PlotColor.BinaryPalette(
            SimpleColors.RED, SimpleColors.FORESTGREEN
        ),
        private val lineType: Int = 1,
        private val customLineWidth: Int? = null
    ) : SeriesStyle() {

        override fun CandleSticks.init() = listOfNotNull(color.rawInit)

        override fun CandleSticks.item(indices: Pair<Int, Int>) = listOf(
            "\"\" using $${indices.first}:${indices.second}:${indices.second + 3}:" +
                    "${indices.second + 1}:${indices.second + 2}:($${indices.second + 2} < $${indices.second} ? -1 : 1) " +
                    "axes x1y2 notitle with candlesticks ${color.raw} lt $lineType lw ${customLineWidth
                        ?: lineWidth}, \\"
        )
    }

    class Line(
        private val color: PlotColor = PlotColor.ByColorName("white"),
        private val customLineWidth: Int? = null,
        private val lineType: Int = 1,
        private val dashType: Int? = null
    ) : SeriesStyle() {

        override fun CandleSticks.item(indices: Pair<Int, Int>) = listOf(
            """"" using ${indices.first}:${indices.second} axes x1y2 notitle with lines 
        ${dashType.let { "dashtype $it " }}lt $lineType lw ${customLineWidth
                ?: lineWidth} lc rgb "${color.raw}", \""".trimIndent()
        )
    }

    class Impulse(
        private val customLineWidth: Int? = null,
        private val lineType: Int = 1,
        private val color: PlotColor = PlotColor.BinaryPalette(
            SimpleColors.RED, SimpleColors.FORESTGREEN
        )
    ) : SeriesStyle() {

        override fun CandleSticks.item(indices: Pair<Int, Int>) = listOf(
            "\"\" using ${indices.first}:${indices.second}:(\$4 < \$2 ? -1 : 1) axes x1y2 notitle with impulses lt $lineType lw ${customLineWidth
                ?: lineWidth} ${color.raw}"
        )
    }

    class CandleTopCharacter : SeriesStyle() {

        override fun CandleSticks.item(indices: Pair<Int, Int>): List<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}