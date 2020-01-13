package de.gapps.utils.observable

import de.gapps.utils.delegates.OnChanged

interface ICachingValueHolder<out T> {

    val value: T

    val previousValues: List<T>

    fun clearCache()
}

open class CachingValueHolder<out T>(initial: T) : ICachingValueHolder<T> {

    override var value: @UnsafeVariance T by OnChanged(initial) {
        previousValues.add(this)
    }

    override val previousValues = ArrayList<@UnsafeVariance T>()

    override fun clearCache() = previousValues.clear()
}