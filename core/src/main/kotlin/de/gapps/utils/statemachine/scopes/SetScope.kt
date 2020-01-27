package de.gapps.utils.statemachine.scopes

import de.gapps.utils.observable.IControllable2
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState


interface ISetScope<E : IEvent, S : IState> {
    infix fun event(event: E)
}

class SetScope<E : IEvent, S : IState>(private val eventHost: IControllable2<IMachineEx<E, S>, E?>) :
    ISetScope<E, S> {
    override fun event(event: E) {
        eventHost.value = event
    }
}