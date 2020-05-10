@file:Suppress("FunctionName")

package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.IOnChangedWritableBase
import dev.zieger.utils.delegates.IScopeFactory
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.delegates.OnChangedBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex


/**
 * Same as [Controllable2] but without a parent type. Use this if you do not care who holds the observed property.
 */
inline fun <T : Any?> Controllable(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    crossinline subscriberStateChanged: (Boolean) -> Unit = {},
    crossinline veto: (@UnsafeVariance T) -> Boolean = { false },
    noinline onControl: Controller<T> = {}
): IControllable<T> = object : IControllable<T>,
    IControllableBase<Any?, T, IControlledChangedScope<T>> by ControllableBase<Any?, T, IControlledChangedScope<T>>(
        initial,
        onlyNotifyOnChanged,
        notifyForInitial,
        storeRecentValues,
        scope,
        mutex,
        ControlledChangedScopeFactory(),
        subscriberStateChanged,
        veto,
        onControl
    ) {}

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
inline fun <P : Any, T : Any?> Controllable2(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    crossinline subscriberStateChanged: (Boolean) -> Unit = {},
    crossinline veto: (@UnsafeVariance T) -> Boolean = { false },
    noinline onControl: IControlledChangedScope2<P, T>.(T) -> Unit = {}
): IControllable2<P, T> = object : IControllable2<P, T>,
    IControllableBase<P, T, IControlledChangedScope2<P, T>> by ControllableBase<P, T, IControlledChangedScope2<P, T>>(
        initial,
        onlyNotifyOnChanged,
        notifyForInitial,
        storeRecentValues,
        scope,
        mutex,
        ControlledChangedScope2Factory(),
        subscriberStateChanged,
        veto,
        onControl
    ) {}

inline fun <P : Any?, T : Any?, S : IControlledChangedScope2<P, T>> ControllableBase(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    scopeFactory: IScopeFactory<P, T, S>,
    crossinline subscriberStateChanged: (Boolean) -> Unit = {},
    crossinline veto: (T) -> Boolean = { false },
    crossinline onControl: S.(T) -> Unit = {},
    onChangedBase: IOnChangedWritableBase<P, T, S> = OnChangedBase(initial, onlyNotifyOnChanged, notifyForInitial,
        storeRecentValues, scope, mutex, scopeFactory, veto, {}),
    base: IObservableWritableBase<P, T, S> = ObservableBase(initial,
        onlyNotifyOnChanged,
        notifyForInitial,
        storeRecentValues,
        scope,
        mutex,
        scopeFactory,
        veto,
        {},
        {},
        onChangedBase
    )
): IControllableBase<P, T, S> = object : IControllableBase<P, T, S>, IObservableWritableBase<P, T, S> by base {

    override val storeRecentValues: Boolean = storeRecentValues
    override val notifyForInitial: Boolean = notifyForInitial
    override val notifyOnChangedValueOnly: Boolean = onlyNotifyOnChanged
    override val scope: CoroutineScope? = scope
    override val mutex: Mutex? = mutex

    private val controller = ArrayList<S.(T) -> Unit>()
    private val controllerS = ArrayList<suspend S.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged(new)
    }

    init {
        control { onControl(it) }
    }

    override fun control(listener: S.(T) -> Unit): () -> Unit {
        controller.add(listener)
        if (notifyForInitial)
            listener.let { createScope(value, null, isInitialNotification = true) { value = it }.it(value) }
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
                    createScope(value, null, isInitialNotification = true) { value = it }.it(value)
                }
            }
        updateSubscriberState()

        return {
            controllerS.remove(listener)
            updateSubscriberState()
        }
    }

    override fun S.onChangedInternal(value: T) {
        base.run { onChangedInternal(value) }
        ArrayList(controller).forEach {
            createScope(value, thisRef, previousValue, previousValues, clearPreviousValues, isInitialNotification) {
                this.value = it
            }.it(value)
        }
        (scope ?: DefaultCoroutineScope()).launchEx(mutex = mutex) {
            ArrayList(controllerS).forEach {
                createScope(
                    value,
                    thisRef,
                    previousValue,
                    previousValues,
                    clearPreviousValues,
                    isInitialNotification
                ) { this@onChangedInternal.value = it }.it(value)
            }
        }
    }

    private fun updateSubscriberState() {
        subscribersAvailable = controller.isNotEmpty() || controllerS.isNotEmpty()
    }
}

