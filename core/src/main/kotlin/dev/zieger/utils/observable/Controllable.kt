package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.IOnChangedScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.delegates.OnChanged2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

typealias Controllable<T> = Controllable2<Any?, T>

/**
 * Container that provides observing of the internal variable of type [T].
 *
 * @param T Type of the internal value.
 * @param initial Initial value for the internal variable. Will not notify any observers.
 * @param onlyNotifyOnChanged Only notify observer when the internal value changes. Active by default.
 * @param storeRecentValues Stores all values and provide them in the onChanged callback. Inactive by default.
 * @param subscriberStateChanged Is invoked when an observer is added or removed.
 * @param onControl Callback that is invoked when the internal values changes.
 */
open class Controllable2<P : Any?, out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    private val notifyForExistingInternal: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onControl: Controller2<P, T> = {}
) : IControllable2<P, T>,
    OnChanged2<P, T>(initial, onlyNotifyOnChanged, false, storeRecentValues, scope, mutex,
        vetoP = veto, onChange = {}) {

    private val controller = ArrayList<Controller2<P, T>>()
    private val controllerS = ArrayList<Controller2S<P, T>>()
    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }

    init {
        @Suppress("LeakingThis")
        control(onControl)
    }

    private fun newControlledChangeScope(
        newValue: @UnsafeVariance T,
        thisRef: P?,
        previousValue: T?,
        previousValues: List<T?>
    ) = ControlledChangedScope(newValue, thisRef, previousValue, previousValues, { clearRecentValues() }) {
        value = it
    }

    override fun control(listener: Controller2<P, T>): () -> Unit {
        controller.add(listener)
        if (notifyForExistingInternal)
            listener.let { newControlledChangeScope(value, null, null, emptyList()).it(value) }
        updateSubscriberState()

        return {
            controller.remove(listener)
            updateSubscriberState()
        }
    }

    override fun controlS(listener: Controller2S<P, T>): () -> Unit {
        controllerS.add(listener)
        if (notifyForExistingInternal)
            listener.let {
                scope?.launchEx(mutex = mutex) {
                    newControlledChangeScope(value, null, null, emptyList()).it(value)
                }
            }
        updateSubscriberState()

        return {
            controllerS.remove(listener)
            updateSubscriberState()
        }
    }

    override fun IOnChangedScope<P, @UnsafeVariance T>.onChanged(value: @UnsafeVariance T) =
        ArrayList(controller).forEach {
            newControlledChangeScope(value, thisRef, previousValue, previousValues).it(value)
        }

    override suspend fun IOnChangedScope<P, @UnsafeVariance T>.onChangedS(value: @UnsafeVariance T) =
        ArrayList(controllerS).forEach {
            newControlledChangeScope(value, thisRef, previousValue, previousValues).it(value)
        }

    private fun updateSubscriberState() {
        subscribersAvailable = controller.isNotEmpty() || controllerS.isNotEmpty()
    }
}

