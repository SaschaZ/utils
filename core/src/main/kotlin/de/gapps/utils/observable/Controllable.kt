package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChanged
import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.log.Log

/**
 * Container that provides observing of the internal variable of type [T].
 *
 */
open class Controllable<out T : Any?> private constructor(
    private val subscriberStateChanged: ((Boolean) -> Unit)? = null,
    private val notifyForExistingInternal: Boolean,
    private val onChangedDelegate: OnChanged<Any?, T>,
    onChanged: IControlledChangedScope<T>.(T) -> Unit = {}
) : IControllable<T>, IOnChanged<Any?, T> by onChangedDelegate {

    /**
     *
     * @param T Type of the internal value.
     * @param initial Initial value for the internal variable. Will not notify any observers.
     * @property onlyNotifyOnChanged Only notify observer when the internal value changes. Active by default.
     * @property storeRecentValues Stores all values and provide them in the onChanged callback. Inactive by default.
     * @property subscriberStateChanged Is invoked when an observer is added or removed.
     * @param onControl Callback that is invoked when the internal values changes.
     */
    constructor(
        initial: T,
        onlyNotifyOnChanged: Boolean = true,
        notifyForExisting: Boolean = false,
        storeRecentValues: Boolean = false,
        subscriberStateChanged: ((Boolean) -> Unit)? = null,
        onControl: IControlledChangedScope<T>.(T) -> Unit = {}
    ) : this(
        subscriberStateChanged, notifyForExisting,
        OnChanged(initial, storeRecentValues, false, onlyNotifyOnChanged), onControl
    )


    private val subscribers = ArrayList<IControlledChangedScope<T>.(T) -> Unit>()

    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }

    init {
        onChangedDelegate.onChange = { onPropertyChanged() }
        @Suppress("LeakingThis")
        control(onChanged)
    }

    private fun newControlledChangeScope(
        newValue: @UnsafeVariance T,
        thisRef: Any?,
        previousValue: T?,
        previousValues: List<T?>
    ) = ControlledChangedScope(newValue, thisRef, previousValue, previousValues, { clearRecentValues() }) {
        //        Log.w("333 new=$it; old=$previousValue; value=$value")
        value = it
    }

    override fun control(listener: IControlledChangedScope<T>.(T) -> Unit): () -> Unit {
        subscribers.add(listener)
        Log.v("value=$value; notifyForExisting=$notifyForExisting")
        if (notifyForExistingInternal) listener.notify()
        updateSubscriberState()

        return {
            subscribers.remove(listener)
            updateSubscriberState()
        }
    }

    private fun (IControlledChangedScope<T>.(T) -> Unit).notify() {
        notify(null, onChangedDelegate.value, null, emptyList())
    }

    private fun (IControlledChangedScope<T>.(T) -> Unit).notify(
        thisRef: Any?,
        new: @UnsafeVariance T,
        old: @UnsafeVariance T?,
        previous: List<@UnsafeVariance T?>
    ) {
        newControlledChangeScope(new, thisRef, old, previous).this(new)
    }

    private fun IOnChangedScope<Any?, @UnsafeVariance T>.onPropertyChanged() {
        subscribers.forEach { it.notify(thisRef, value, previousValue, previousValues) }
    }

    private fun updateSubscriberState() {
        subscribersAvailable = subscribers.size > 1
    }
}

