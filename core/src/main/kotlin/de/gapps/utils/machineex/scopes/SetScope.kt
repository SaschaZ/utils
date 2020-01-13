package de.gapps.utils.machineex.scopes

import de.gapps.utils.machineex.IEvent
import de.gapps.utils.machineex.IState
import de.gapps.utils.observable.IControllable


interface ISetScope<E : IEvent, S : IState> {
    infix fun event(event: E)
}

class SetScope<E : IEvent, S : IState>(private val eventHost: IControllable<E?>) :
    ISetScope<E, S> {
    override fun event(event: E) {
        eventHost.value = event
    }
}