@file:Suppress("LeakingThis")

package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.IScope2Factory
import dev.zieger.utils.delegates.IScopeFactory
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
    override val notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onControl: Controller<T> = {}
) : IControllable<T>, ControllableBase<Any?, T, IControlledChangedScope<T>>(
    initial, onlyNotifyOnChanged, notifyForInitial, storeRecentValues, scope, mutex,
    subscriberStateChanged, ControlledChangedScopeFactory(), veto, onControl
)

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
open class Controllable2<P : Any?, out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override val notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onControl: IControlledChangedScope2<P, T>.(T) -> Unit = {}
) : IControllable2<P, T>, ControllableBase<P, T, IControlledChangedScope2<P, T>>(
    initial,
    onlyNotifyOnChanged,
    notifyForInitial,
    storeRecentValues,
    scope,
    mutex,
    subscriberStateChanged,
    ControlledChangedScope2Factory(),
    veto,
    onControl
)

abstract class ControllableBase<P : Any?, out T : Any?, out S : IControlledChangedScope2<P, T>>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override val notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    scopeFactory: IScope2Factory<P, T, S>,
    veto: (@UnsafeVariance T) -> Boolean = { false },
    onControl: S.(T) -> Unit = {}
) : IControllableBase<P, T, S>,
    OnChangedBase<P, T, S>(initial, onlyNotifyOnChanged, false, storeRecentValues,
        scope, mutex, scopeFactory, veto, {}, {}) {

    private val controller = ArrayList<S.(T) -> Unit>()
    private val controllerS = ArrayList<suspend S.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }

    init {
        control(onControl)
    }

    override fun control(listener: S.(T) -> Unit): () -> Unit {
        controller.add(listener)
        if (notifyForInitial)
            listener.let {
                createScope(value, previousThisRef.get(), null, recentValues,
                    { recentValues.clear() }, true, { value = it }).it(value)
            }
        updateSubscriberState()

        return {
            controller.remove(listener)
            updateSubscriberState()
        }
    }

    override fun controlS(listener: suspend S.(T) -> Unit): () -> Unit {
        controllerS.add(listener)
        if (notifyForInitial)
            listener.let {
                scope?.launchEx(mutex = mutex) {
                    createScope(value, previousThisRef.get(), null, recentValues,
                        { recentValues.clear() }, true, { value = it }).it(value)
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
            createScope(value, previousThisRef.get(), null, recentValues,
                { recentValues.clear() }, true, { this.value = it }).it(value)
        }

    override suspend fun (@UnsafeVariance S).onChangedSInternal(value: @UnsafeVariance T) =
        ArrayList(controllerS).forEach {
            createScope(value, previousThisRef.get(), null, recentValues,
                { recentValues.clear() }, true, { this.value = it }).it(value)
        }

    private fun updateSubscriberState() {
        subscribersAvailable = controller.isNotEmpty() || controllerS.isNotEmpty()
    }
}

open class ControlledChangedScopeFactory<out T : Any?> :
    IScopeFactory<T, IControlledChangedScope<@UnsafeVariance T>> {
    override fun createScope(
        value: @UnsafeVariance T,
        thisRef: Any?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean,
        setter: (T) -> Unit
    ): IControlledChangedScope<T> =
        ControlledChangedScope(
            value, thisRef, previousValue, previousValues, clearPreviousValues, setter,
            isInitialNotification
        )
}

open class ControlledChangedScope2Factory<P : Any?, out T : Any?> :
    IScope2Factory<P, T, IControlledChangedScope2<P, @UnsafeVariance T>> {
    override fun createScope(
        value: @UnsafeVariance T,
        thisRef: P?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean,
        setter: (T) -> Unit
    ): IControlledChangedScope2<P, T> =
        ControlledChangedScope2(
            value, thisRef, previousValue, previousValues, clearPreviousValues, setter,
            isInitialNotification
        )
}
