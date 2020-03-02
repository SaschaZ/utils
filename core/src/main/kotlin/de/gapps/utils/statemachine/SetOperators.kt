package de.gapps.utils.statemachine


operator fun IEvent.unaryPlus(): Set<IEvent> = setOf(this)
operator fun IEvent.plus(other: IEvent): Set<IEvent> = setOf(this, other)

operator fun IState.unaryPlus(): Set<IState> = setOf(this)
operator fun IState.plus(other: IState): Set<IState> = setOf(this, other)
