@file:Suppress("unused")

package de.gapps.utils.ta.input.currency


import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.IDurationEx

interface ICandleParameter {

    val pair: SCurrencyPair
    val interval: IDurationEx
    val range: ClosedRange<ITimeEx>

    var partial: Boolean
    var reverse: Boolean

    val intervalPretty: String
    val stringPretty: String
}

sealed class SCandleParameter : ICandleParameter {

    companion object {

        const val DEFAULT_CANDLE_COUNT = 750
    }

    override var partial: Boolean = true
    override var reverse: Boolean = true

    override val intervalPretty: String
        get() = interval.formatDuration()

    override val stringPretty: String
        get() = "${pair.toString().replace("/", "")}-${intervalPretty}"

    override fun equals(other: Any?) =
        (other as? SCandleParameter)?.let { pair == it.pair && interval == it.interval && range == it.range } ?: false

    override fun hashCode() = pair.hashCode() / 3 + interval.hashCode() / 3 + range.hashCode() / 3

    override fun toString() =
        "CandleParameter($pair/${intervalPretty}, candlesToFetch=${((range.endInclusive - range.start).minutes / interval.minutes)}, " +
                "startTime=${range.start}, endTime=${range.endInclusive})"
}

open class CandleParameterInternal(
    override val pair: CurrencyPairInternal,
    override val interval: IDurationEx,
    override val range: ClosedRange<ITimeEx>
) : SCandleParameter() {

    constructor(
        pair: CurrencyPairInternal,
        interval: IDurationEx,
        candlesToFetch: Int = DEFAULT_CANDLE_COUNT,
        fetchStart: ITimeEx = (TimeEx() - interval * candlesToFetch)
    ) : this(pair, interval, fetchStart..TimeEx())

    fun copy(
        pair: CurrencyPairInternal = this.pair,
        interval: IDurationEx = this.interval,
        candlesToFetch: Int = DEFAULT_CANDLE_COUNT,
        fetchStart: ITimeEx = (TimeEx() - interval * candlesToFetch)
    ) = CandleParameterInternal(pair, interval, fetchStart..TimeEx())
}

open class CandleParameterExternal(
    override val pair: CurrencyPairExternal,
    override val interval: IDurationEx,
    override val range: ClosedRange<ITimeEx>
) : SCandleParameter() {

    constructor(
        pair: CurrencyPairExternal,
        interval: IDurationEx,
        candlesToFetch: Int = DEFAULT_CANDLE_COUNT,
        fetchStart: ITimeEx = TimeEx() - interval * candlesToFetch
    ) : this(pair, interval, fetchStart..TimeEx())

    fun copy(
        pair: CurrencyPairExternal = this.pair,
        interval: IDurationEx = this.interval,
        candlesToFetch: Int = DEFAULT_CANDLE_COUNT,
        fetchStart: ITimeEx = (TimeEx() - interval * candlesToFetch)
    ) =
        CandleParameterExternal(pair, interval, fetchStart..TimeEx())
}

fun <T : SCurrencyPair> T.toCandleParameter(
    interval: IDurationEx,
    candlesToFetch: Int = SCandleParameter.DEFAULT_CANDLE_COUNT,
    fetchStart: ITimeEx = TimeEx() - interval * candlesToFetch
) = when (this) {
    is CurrencyPairExternal -> CandleParameterExternal(
        this,
        interval,
        candlesToFetch,
        fetchStart
    )
    is CurrencyPairInternal -> CandleParameterInternal(
        this,
        interval,
        candlesToFetch,
        fetchStart
    )
    else -> throw IllegalArgumentException("unknown currency pair type $this")
}