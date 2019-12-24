package de.gapps.utils.ta.input.currency

import de.gapps.utils.ta.input.currency.SCandleParameter.Companion.DEFAULT_CANDLE_COUNT
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.IDurationEx

enum class ECurrencyPairs(val currencyPair: CurrencyPairExternal) {

    BTCUSD(
        CurrencyPairExternal(
            ECurrencies.BTC,
            ECurrencies.USD
        )
    ),
    ETHUSD(
        CurrencyPairExternal(
            ECurrencies.ETH,
            ECurrencies.USD
        )
    ),
    ETHBTC(
        CurrencyPairExternal(
            ECurrencies.ETH,
            ECurrencies.BTC
        )
    ),
    TRXBTC(
        CurrencyPairExternal(
            ECurrencies.TRX,
            ECurrencies.BTC
        )
    ),
    XRPBTC(
        CurrencyPairExternal(
            ECurrencies.XRP,
            ECurrencies.BTC
        )
    ),
    LTCBTC(
        CurrencyPairExternal(
            ECurrencies.LTC,
            ECurrencies.BTC
        )
    ),
    ADABTC(
        CurrencyPairExternal(
            ECurrencies.ADA,
            ECurrencies.BTC
        )
    ),
    BCHBTC(
        CurrencyPairExternal(
            ECurrencies.BCH,
            ECurrencies.BTC
        )
    ),
    EOSBTC(
        CurrencyPairExternal(
            ECurrencies.EOS,
            ECurrencies.BTC
        )
    ),
    BXBT(CurrencyPairExternal("BXBT"));

    val base = currencyPair.base

    val counter = currencyPair.counter

    fun toCandleParameter(
        interval: IDurationEx,
        candlesToFetch: Int = DEFAULT_CANDLE_COUNT,
        start: ITimeEx = TimeEx() - interval * candlesToFetch
    ) = CandleParameterExternal(currencyPair, interval, candlesToFetch, start)

    override fun toString() = "$currencyPair"
}