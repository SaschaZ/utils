package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.BaseType.Event
import de.gapps.utils.statemachine.BaseType.State
import de.gapps.utils.statemachine.scopes.ExecutorScope

interface IMachineOperators : IMachineEx {

    suspend operator fun BaseType.unaryPlus(): ValueDataHolder = ValueDataHolder(this)

    suspend operator fun BaseType.unaryMinus(): Set<ValueDataHolder> = setOf(ValueDataHolder(this))


    // link items with + operator
    suspend operator fun Set<ValueDataHolder>.plus(other: BaseType): Set<ValueDataHolder> =
        setOf(*this.toTypedArray(), ValueDataHolder(other))


    // apply Data with * operator
    suspend operator fun BaseType.times(data: Data?) =
        ValueDataHolder(this, data)

    suspend operator fun ValueDataHolder.times(data: Data?) =
        apply { this.data = data }

    suspend operator fun Set<ValueDataHolder>.times(data: Data?) = map {
        it.apply { this.data = data }
    }.toSet()


    suspend operator fun Set<ValueDataHolder>.plusAssign(state: State) {
        this += ValueDataHolder(state)
    }

    suspend operator fun Set<ValueDataHolder>.plusAssign(state: ValueDataHolder) {
        this += { state }
    }

    suspend operator fun Set<ValueDataHolder>.timesAssign(block: suspend ExecutorScope.() -> State?) {
        mapper.addMapping(events, states) { event, state ->
            ValueDataHolder(ExecutorScope(event, state).block() as State)
        }
    }

    suspend operator fun Set<ValueDataHolder>.plusAssign(block: suspend ExecutorScope.() -> ValueDataHolder?) {
        mapper.addMapping(events, states) { event, state -> ExecutorScope(event, state).block() }
    }

    suspend operator fun Set<ValueDataHolder>.minusAssign(block: suspend ExecutorScope.() -> Unit) {
        mapper.addMapping(events, states) { event, state -> ExecutorScope(event, state).block(); state }
    }
}

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder>.events
    get() = filter { it.value is Event }.map { it }.toSet()

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder>.states
    get() = filter { it.value is State }.map { it }.toSet()