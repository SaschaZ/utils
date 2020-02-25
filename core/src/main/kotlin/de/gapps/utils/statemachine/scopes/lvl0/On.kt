package de.gapps.utils.statemachine.scopes.lvl0

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.IMachineExScope


interface IOnScope<out E : IEvent, out S : IState> {
    val machineExScope: IMachineExScope<E, S>
}

class OnScope<out E : IEvent, out S : IState>(override val machineExScope: IMachineExScope<E, S>) :
    IOnScope<E, S>

val <E : IEvent, S : IState> IMachineExScope<E, S>.on: IOnScope<E, S>
    get() = OnScope(this)