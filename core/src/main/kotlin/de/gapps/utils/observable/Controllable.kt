package de.gapps.utils.observable

import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.misc.lastOrNull

/**
 * Container that provides observing of the internal variable of type [T].
 *
 * @param T Type of the internal value.
 * @param initial Initial value for the internal variable. Will not notify any observers.
 * @property onlyNotifyOnChanged Only notify observer when the internal value changes. Active by default.
 * @property storeRecentValues Stores all values and provide them in the onChanged callback. Inactive by default.
 * @property subscriberStateChanged Is invoked when an observer is added or removed.
 * @param onChanged Callback that is invoked when the internal values changes.
 */
open class Controllable<out T>(
    initial: T,
    private val onlyNotifyOnChanged: Boolean = true,
    private val notifyForExisting: Boolean = false,
    private val storeRecentValues: Boolean = false,
    private val subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: ControlObserver<T> = {}
) : OnChanged<Any, T>(initial), IControllable<T> {

    private val subscribers = ArrayList<ControlObserver<T>>()

    override var value: @UnsafeVariance T
        get() = valueField
        set(newValue) {
            valueField = newValue
        }

    init {
        @Suppress("LeakingThis")
        control(onChanged)
    }

    private fun newControlledChangeScope(
        newValue: @UnsafeVariance T,
        previousValue: T?,
        previousValues: List<T>
    ) = ControlledChangedScope(
        newValue,
        previousValue, previousValues
    ) {
        value = it
    }

    override fun control(listener: ControlObserver<T>): () -> Unit {
        subscribers.add(listener)
        if (notifyForExisting)
            newControlledChangeScope(value, recentValues.lastOrNull(), recentValues).listener(value)
        if (subscribers.size > 1) updateSubscriberState()
        return {
            subscribers.remove(listener)
            updateSubscriberState()
        }
    }

    private fun updateSubscriberState() {
        subscribersAvailable = subscribers.size > 1
    }

    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }
}

