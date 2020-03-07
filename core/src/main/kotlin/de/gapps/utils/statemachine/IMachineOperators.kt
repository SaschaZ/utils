package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.BaseType.*
import de.gapps.utils.statemachine.scopes.ExecutorScope

interface IMachineOperators : IMachineEx {

    // start condition with + (single value) or - sign (set)
    suspend operator fun BaseType.unaryPlus(): ValueDataHolder = ValueDataHolder(this)
    suspend operator fun BaseType.unaryMinus(): Set<ValueDataHolder> = setOf(ValueDataHolder(this))


    // link items with + operator
    suspend operator fun Set<ValueDataHolder>.plus(other: BaseType): Set<ValueDataHolder> =
        setOf(*this.toTypedArray(), ValueDataHolder(other))


    // apply Data with * operator SUSPENDED
    suspend operator fun BaseType.times(data: Data?) =
        ValueDataHolder(this, data.toSet)

    suspend operator fun ValueDataHolder.times(data: Data?) =
        apply { this.data = data.toSet }

    suspend operator fun Set<ValueDataHolder>.times(data: Data?) = map {
        it.apply { this.data = data.toSet }
    }.toSet()

    // apply Data with / operator UNSUSPENDED
    operator fun BaseType.div(data: Data?) =
        ValueDataHolder(this, data.toSet)

    operator fun ValueDataHolder.div(data: Data?) =
        apply { this.data = data.toSet }

    operator fun Set<ValueDataHolder>.div(data: Data?) = map {
        it.apply { this.data = data.toSet }
    }.toSet()

    // define action and/or new state with assign operators
    // action wih new state +=
    suspend operator fun Set<ValueDataHolder>.plusAssign(state: State) {
        this += ValueDataHolder(state)
    }

    // action with new state and data +=
    suspend operator fun Set<ValueDataHolder>.plusAssign(state: ValueDataHolder) {
        this += { state }
    }

    // suspended with optional new state and data +=
    suspend operator fun Set<ValueDataHolder>.plusAssign(block: suspend ExecutorScope.() -> ValueDataHolder?) {
        mapper.addMapping(events, states) { event, state -> ExecutorScope(event, state).block() }
    }

    // suspended action with optional new state *=
    suspend operator fun Set<ValueDataHolder>.timesAssign(block: suspend ExecutorScope.() -> State?) {
        mapper.addMapping(events, states) { event, state ->
            ValueDataHolder(ExecutorScope(event, state).block() as State)
        }
    }

    // suspended action only
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