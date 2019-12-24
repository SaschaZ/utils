package de.gapps.utils.ta.input.currency

enum class ECurrencies(val currency: Currency) {

    BTC(Currency("BTC")),
    USD(Currency("USD")),
    ETH(Currency("ETH")),
    TRX(Currency("TRX")),
    XRP(Currency("XRP")),
    LTC(Currency("LTC")),
    ADA(Currency("ADA")),
    BCH(Currency("BCH")),
    EOS(Currency("EOS"));

    val currencyName = currency.currencyName

    override fun toString() = "$currency"
}