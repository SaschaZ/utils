@file:Suppress("MemberVisibilityCanBePrivate", "unused", "SpellCheckingInspection")

package de.gapps.utils.ta.input.currency


sealed class SCurrencyPair(
    val base: Currency,
    val counter: Currency?
) {

    val isExternal: Boolean
        get() = this is CurrencyPairExternal
    val isInternal: Boolean
        get() = this is CurrencyPairInternal

    open fun raw() = "$base${counter?.let { "$it" } ?: ""}"

    override fun equals(other: Any?) =
        (other as? SCurrencyPair)?.let { base == it.base && counter == it.counter } ?: false

    override fun hashCode() = base.hashCode() / 2 + (counter?.let { it.hashCode() / 2 } ?: 0)
    override fun toString() = "$base${counter?.let { "/$it" }}"
}

open class CurrencyPairExternal(
    base: Currency,
    counter: Currency? = null
) : SCurrencyPair(base, counter) {
    constructor(base: String, counter: String? = null) : this(
        Currency(
            base
        ), counter?.let {
            Currency(
                it
            )
        })

    constructor(base: ECurrencies, counter: ECurrencies? = null) : this(base.currency, counter?.currency)
}

open class CurrencyPairInternal(
    base: Currency,
    counter: Currency? = null
) : SCurrencyPair(base, counter) {
    constructor(base: String, counter: String? = null) : this(
        Currency(
            base
        ), counter?.let {
            Currency(
                it
            )
        })

    constructor(base: ECurrencies, counter: ECurrencies? = null) : this(base.currency, counter?.currency)
}
