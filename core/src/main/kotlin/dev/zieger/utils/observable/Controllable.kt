package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.delegates.OnChangedBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex


/**
 * Same as [Controllable2] but without a parent type. Use this if you do not care who holds the observed property.
 */
open class Controllable<out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override val notifyForExisting: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onControl: Controller<T> = {}
) : IControllable<T>, ControllableBase<Any?, T, IControlledChangedScope<T>>(
    initial, onlyNotifyOnChanged, notifyForExisting, storeRecentValues, scope, mutex,
    subscriberStateChanged, veto, onControl
) {
    override fun newOnChangedScope(
        newValue: @UnsafeVariance T,
        previousValue: @UnsafeVariance T?
    ): IControlledChangedScope<T> = ControlledChangedScope(newValue, previousThisRef.get(), previousValue, recentValues,
        { clearRecentValues() }, { })
}

/**
 * Container that provides observing of the delegated property of type [T].
 *
 * @param T Type of the internal value.
 * @param initial Initial value for the internal variable. Will not notify any observers.
 * @param onlyNotifyOnChanged Only notify observer when the internal value changes. Active by default.
 * @param storeRecentValues Stores all values and provide them in the onChanged callback. Inactive by default.
 * @param subscriberStateChanged Is invoked when an observer is added or removed.
 * @param onControl Callback that is invoked when the internal values changes.
 */
open class Controllable2<P : Any, out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override val notifyForExisting: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onControl: IControlledChangedScope2<P, T>.(T) -> Unit = {}
) : IControllable2<P, T>, ControllableBase<P, T, IControlledChangedScope2<P, T>>(
    initial,
    onlyNotifyOnChanged,
    notifyForExisting,
    storeRecentValues,
    scope,
    mutex,
    subscriberStateChanged,
    veto,
    onControl
) {

    override fun newOnChangedScope(
        newValue: @UnsafeVariance T,
        previousValue: @UnsafeVariance T?
    ): IControlledChangedScope2<P, T> =
        ControlledChangedScope2(newValue, previousThisRef.get(), previousValue, recentValues, { clearRecentValues() }) {
            value = it
        }
}

abstract class ControllableBase<P : Any?, out T : Any?, out S : IControlledChangedScope2<P, T>>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override val notifyForExisting: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onControl: S.(T) -> Unit = {}
) : IControllableBase<P, T, S>,
    OnChangedBase<P, T, S>(initial, onlyNotifyOnChanged, false, storeRecentValues,
        scope, mutex, veto, onChanged = {}) {

    private val controller = ArrayList<S.(T) -> Unit>()
    private val controllerS = ArrayList<suspend S.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }

    init {
        @Suppress("LeakingThis")
        control(onControl)
    }

    override fun control(listener: S.(T) -> Unit): () -> Unit {
        controller.add(listener)
        if (notifyForExisting)
            listener.let { newOnChangedScope(value, null).it(value) }
        updateSubscriberState()

        return {
            controller.remove(listener)
            updateSubscriberState()
        }
    }

    override fun controlS(listener: suspend S.(T) -> Unit): () -> Unit {
        controllerS.add(listener)
        if (notifyForExisting)
            listener.let {
                scope?.launchEx(mutex = mutex) {
                    newOnChangedScope(value, null).it(value)
                }
            }
        updateSubscriberState()

        return {
            controllerS.remove(listener)
            updateSubscriberState()
        }
    }

    override fun (@UnsafeVariance S).onChangedInternal(value: @UnsafeVariance T) =
        ArrayList(controller).forEach {
            newOnChangedScope(value, previousValue).it(value)
        }

    override suspend fun (@UnsafeVariance S).onChangedSInternal(value: @UnsafeVariance T) =
        ArrayList(controllerS).forEach {
            newOnChangedScope(value, previousValue).it(value)
        }

    private fun updateSubscriberState() {
        subscribersAvailable = controller.isNotEmpty() || controllerS.isNotEmpty()
    }
}

