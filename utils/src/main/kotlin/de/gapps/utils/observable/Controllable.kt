package de.gapps.utils.observable

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.misc.lastOrNull
import de.gapps.utils.misc.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KProperty

/**
 * Container that provides observing of the internal variable of type [T].
 *
 * @param T Type of the internal value.
 * @param initial Initial value for the internal variable. Will not notify any observers.
 * @property scope [CoroutineScope] That is used to notify asynchronous observers. By default a scope with the
 * [Dispatchers.Default] Coroutine context is used.
 * @property onlyNotifyOnChanged Only notify observer when the internal value changes. Active by default.
 * @property storeRecentValues Stores all values and provide them in the onChanged callback. Inactive by default.
 * @property subscriberStateChanged Is invoked when an observer is added or removed.
 * @param onChanged Callback that is invoked when the internal values changes.
 */
open class Controllable<out T>(
    initial: T,
    private val scope: CoroutineScope =
        DefaultCoroutineScope(Controllable::class.name),
    private val onlyNotifyOnChanged: Boolean = true,
    private val storeRecentValues: Boolean = false,
    private val subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: ControlObserver<T> = {}
) : CachingValueHolder<T>(initial), IControllable<T> {

    private val subscribers = ArrayList<ControlObserver<T>>()

    override fun getValue(thisRef: Any, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: @UnsafeVariance T) {
        this.value = value
    }

    override var value: @UnsafeVariance T = initial
        set(newValue) {
            if (newValue != field || !onlyNotifyOnChanged) {
                super.value = newValue
                field = newValue

                ArrayList(subscribers).forEach {
                    newControlledChangeScope(newValue, previousValues.lastOrNull(), previousValues).it(newValue)
                }
            }
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
        newControlledChangeScope(value, previousValues.lastOrNull(), previousValues).listener(value)
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

