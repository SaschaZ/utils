package de.gapps.utils.statemachine.scopes

import de.gapps.utils.observable.IControllable
import de.gapps.utils.statemachine.IEvent


interface ISetScope<out K : Any, out V : Any, out E : IEvent<K, V>> : IEvent<K, V> {
    val event: E
    infix fun event(event: @UnsafeVariance E): ISetScope<K, V, IEvent<K, V>>
}

class SetScope<out K : Any, out V : Any, out E : IEvent<K, V>>(private val eventHost: IControllable<E>) :
    ISetScope<K, V, E>,
    MutableMap<@UnsafeVariance K, @UnsafeVariance V> by HashMap() {

    override val event: E
        get() = eventHost.value

    override fun event(event: @UnsafeVariance E): ISetScope<K, V, E> {
        eventHost.value = event
        return this
    }
}