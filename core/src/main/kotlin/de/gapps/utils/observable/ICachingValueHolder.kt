package de.gapps.utils.observable

import de.gapps.utils.delegates.OnChanged

interface ICachingValueHolder<out T> {

    val value: T

    val previousValues: List<T>
}

open class CachingValueHolder<out T>(initial: T) : ICachingValueHolder<T> {

    override lateinit var previousValues: List<@UnsafeVariance T>

    override var value: @UnsafeVariance T by OnChanged(initial, true) {
        this@CachingValueHolder.previousValues = previousValues
    }
}