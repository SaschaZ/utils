package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.scopes.ExecutorScope

interface IMachineOperators : IMachineEx {

    suspend operator fun Any.unaryPlus(): ValueDataHolder<*> = ValueDataHolder(this)

    suspend operator fun Any.unaryMinus(): Set<ValueDataHolder<*>> = setOf(ValueDataHolder(this))

    suspend operator fun Set<ValueDataHolder<*>>.div(other: Any): Set<ValueDataHolder<*>> =
        setOf(*this.toTypedArray(), ValueDataHolder(other))
    suspend operator fun Set<ValueDataHolder<*>>.times(other: Any): Set<ValueDataHolder<*>> =
        setOf(*this.toTypedArray(), ValueDataHolder(other))


    suspend operator fun Event.plus(data: Any?) =
        ValueDataHolder(this, data)
    suspend operator fun Set<Event>.plus(data: Any?) = map {
        ValueDataHolder(
            it,
            data
        )
    }.toSet()


    suspend operator fun Set<ValueDataHolder<*>>.plusAssign(state: State) { this += ValueDataHolder(state) }
    suspend operator fun Set<ValueDataHolder<*>>.plusAssign(state: ValueDataHolder<State>) { this += { state } }
    suspend operator fun Set<ValueDataHolder<*>>.timesAssign(block: suspend ExecutorScope.() -> State?)  {
        mapper.addMapping(events, states) { event, state ->
            ValueDataHolder(ExecutorScope(event, state).block() as State)
        }
    }
    suspend operator fun Set<ValueDataHolder<*>>.plusAssign(block: suspend ExecutorScope.() -> ValueDataHolder<State>?)  {
        mapper.addMapping(events, states) { event, state -> ExecutorScope(event, state).block() }
    }
    suspend operator fun Set<ValueDataHolder<*>>.minusAssign(block: suspend ExecutorScope.() -> Unit)  {
        mapper.addMapping(events, states){ event, state -> ExecutorScope(event, state).block(); state }
    }
}

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder<*>>.events
    get() = filter { it.value is Event }.map { it as ValueDataHolder<Event> }.toSet()

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder<*>>.states
    get() = filter { it.value is State }.map { it as ValueDataHolder<State> }.toSet()