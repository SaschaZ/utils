@file:Suppress("ClassName")

package de.gapps.utils.ta.processing

import de.gapps.utils.ta.output.chart.styles.ISeriesStyle


sealed class SSeriesStyleType {

    open val styles: List<ISeriesStyle<*>> = emptyList()

    // TODO define default styles
    data class CANDLE(override val styles: List<ISeriesStyle<*>> = emptyList()) : SSeriesStyleType()

    data class IMPULSE(override val styles: List<ISeriesStyle<*>> = emptyList()) : SSeriesStyleType()
    data class SINGLE_LINE(override val styles: List<ISeriesStyle<*>> = emptyList()) : SSeriesStyleType()
    data class TRIPLE_LINE(override val styles: List<ISeriesStyle<*>> = emptyList()) : SSeriesStyleType()
    data class TD(override val styles: List<ISeriesStyle<*>> = emptyList()) : SSeriesStyleType()
}