package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState


interface IOnScope<out E : IEvent<*, *>, out S : IState> {
    val machineExScope: IMachineExScope<E, S>
}

class OnScope<out E : IEvent<*, *>, out S : IState>(override val machineExScope: IMachineExScope<E, S>) : IOnScope<E, S>