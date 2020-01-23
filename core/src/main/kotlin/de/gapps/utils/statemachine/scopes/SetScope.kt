package de.gapps.utils.statemachine.scopes

import de.gapps.utils.observable.IControllable
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState


interface ISetScope<E : IEvent, S : IState> {
    infix fun event(event: E)
}

class SetScope<E : IEvent, S : IState>(private val eventHost: IControllable<Any, E?>) :
    ISetScope<E, S> {
    override fun event(event: E) {
        eventHost.value = event
    }
}