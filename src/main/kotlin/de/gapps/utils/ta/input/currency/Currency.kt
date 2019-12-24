@file:Suppress("unused", "SpellCheckingInspection")

package de.gapps.utils.ta.input.currency

data class Currency(val currencyName: String) {

    constructor(currency: ECurrencies) : this(currency.name)

    override fun toString() = currencyName.toUpperCase()
}