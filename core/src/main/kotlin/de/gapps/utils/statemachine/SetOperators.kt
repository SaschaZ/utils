package de.gapps.utils.statemachine


operator fun <D : Any> IEvent<D>.unaryPlus(): Set<IEvent<D>> = setOf(this)
operator fun <D : Any> IEvent<D>.times(other: IEvent<D>): Set<IEvent<D>> = setOf(this, other)
operator fun <D : Any> Set<IEvent<D>>.times(other: IEvent<D>): Set<IEvent<D>> = setOf(*this.toTypedArray(), other)

operator fun <D : Any> IState<D>.unaryPlus(): Set<IState<D>> = setOf(this)
operator fun <D : Any> IState<D>.times(other: IState<D>): Set<IState<D>> = setOf(this, other)
operator fun <D : Any> Set<IState<D>>.times(other: IState<D>): Set<IState<D>> = setOf(*this.toTypedArray(), other)
