package de.gapps.utils.statemachine.scopes

import de.gapps.utils.observable.IControllable
import de.gapps.utils.statemachine.IEvent


interface ISetScope<E : IEvent> {
    infix fun event(event: E)
}

class SetScope<E : IEvent>(private val eventHost: IControllable<E>) : ISetScope<E> {
    override fun event(event: E) {
        eventHost.value = event
    }
}