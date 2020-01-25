package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChanged2
import de.gapps.utils.delegates.IOnChangedScope
import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.delegates.OnChanged2
import de.gapps.utils.log.Log

typealias Controllable<T> = Controllable2<Any?, T>

/**
 * Container that provides observing of the internal variable of type [T].
 *
 */
open class Controllable2<out P : Any?, out T : Any?> private constructor(
    private val subscriberStateChanged: ((Boolean) -> Unit)? = null,
    private val notifyForExistingInternal: Boolean,
    private val onChangedDelegate: OnChanged2<P, T>,
    onChanged: Controller2<P, T> = {}
) : IControllable2<P, T>, IOnChanged2<@UnsafeVariance P, T> by onChangedDelegate {

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
        onControl: Controller2<P, T> = {}
    ) : this(
        subscriberStateChanged, notifyForExisting,
        OnChanged2(initial, storeRecentValues, false, onlyNotifyOnChanged), onControl
    )


    private val subscribers = ArrayList<Controller2<P, T>>()

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
        thisRef: P?,
        previousValue: T?,
        previousValues: List<T?>
    ) = ControlledChangedScope(newValue, thisRef, previousValue, previousValues, { clearRecentValues() }) {
        //        Log.w("333 new=$it; old=$previousValue; value=$value")
        value = it
    }

    override fun control(listener: Controller2<P, T>): () -> Unit {
        subscribers.add(listener)
        Log.v("value=$value; notifyForExisting=$notifyForExisting")
        if (notifyForExistingInternal) listener.notify()
        updateSubscriberState()

        return {
            subscribers.remove(listener)
            updateSubscriberState()
        }
    }

    private fun Controller2<P, T>.notify() {
        notify(null, onChangedDelegate.value, null, emptyList())
    }

    private fun Controller2<P, T>.notify(
        thisRef: P?,
        new: @UnsafeVariance T,
        old: @UnsafeVariance T?,
        previous: List<@UnsafeVariance T?>
    ) {
        newControlledChangeScope(new, thisRef, old, previous).this(new)
    }

    private fun IOnChangedScope<P, @UnsafeVariance T>.onPropertyChanged() {
        subscribers.forEach { it.notify(thisRef, value, previousValue, previousValues) }
    }

    private fun updateSubscriberState() {
        subscribersAvailable = subscribers.size > 1
    }
}

