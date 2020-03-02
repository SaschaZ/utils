package de.gapps.utils.statemachine


operator fun <D : IData, E : IEvent<D>> E.unaryPlus(): Set<E> = setOf(this)
operator fun <D : IData, E : IEvent<D>> E.plus(other: E): Set<E> = setOf(this, other)

operator fun <S : IState> S.unaryPlus(): Set<S> = setOf(this)
operator fun <S : IState> S.plus(other: S): Set<S> = setOf(this, other)
