@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.BaseType.Event
import de.gapps.utils.statemachine.BaseType.State
import de.gapps.utils.statemachine.scopes.ExecutorScope

data class EntryBuilder(
    val events: MutableSet<ValueDataHolder>,
    val states: MutableSet<ValueDataHolder>,
    val isStateCondition: Boolean
)

interface IMachineOperators : IMachineEx {

    // start entry with +
    suspend operator fun BaseType.unaryMinus(): EntryBuilder = EntryBuilder(
        (this as? Event)?.let { mutableSetOf(ValueDataHolder(it)) } ?: HashSet(),
        (this as? State)?.let { mutableSetOf(ValueDataHolder(it)) } ?: HashSet(),
        this is State
    )


    // link items with + operator
    suspend operator fun EntryBuilder.plus(other: BaseType): EntryBuilder {
        when (other) {
            is State -> states.add(ValueDataHolder(other))
            is Event -> events.add(ValueDataHolder(other))
            else -> throw IllegalArgumentException(
                "Invalid type. Only states and events can be linked together. (is ${this::class.name})"
            )
        }
        return this
    }

    suspend operator fun EntryBuilder.plus(other: ValueDataHolder): EntryBuilder {
        when {
            other.value is State -> states.add(other)
            other.value is Event -> events.add(other)
            else -> throw IllegalArgumentException(
                "Invalid type. Only states and events can be linked together. (is ${other::class.name})"
            )
        }
        return this
    }

    suspend operator fun EntryBuilder.minus(other: BaseType): EntryBuilder {
        when (other) {
            is State -> states.add(ValueDataHolder(other, exclude = true))
            is Event -> events.add(ValueDataHolder(other, exclude = true))
            else -> throw IllegalArgumentException(
                "Invalid type. Only states and events can be linked together. (is ${this::class.name})"
            )
        }
        return this
    }


    // apply Data with * operator SUSPENDED
    suspend operator fun BaseType.times(data: Data?) =
        ValueDataHolder(this, data.toSet)

    suspend operator fun ValueDataHolder.times(data: Data?) =
        apply { this.data = data.toSet }

    suspend operator fun EntryBuilder.times(data: Data?) = apply {
        (events + states).map {
            it.apply { this.data = data.toSet }
        }
    }

    // apply Data with / operator UNSUSPENDED
    operator fun BaseType.div(data: Data?) =
        ValueDataHolder(this, data.toSet)

    operator fun ValueDataHolder.div(data: Data?) =
        apply { this.data = data.toSet }

    operator fun EntryBuilder.div(data: Data?) = apply {
        (events + states).map {
            it.apply { this.data = data.toSet }
        }.toSet()
    }

    // define action and/or new state with assign operators
    // action wih new state +=
    suspend operator fun EntryBuilder.plusAssign(state: State) {
        this += ValueDataHolder(state)
    }

    // action with new state and data +=
    suspend operator fun EntryBuilder.plusAssign(state: ValueDataHolder) {
        this += { state }
    }

    // suspended with optional new state and data +=
    suspend operator fun EntryBuilder.plusAssign(block: suspend ExecutorScope.() -> ValueDataHolder?) {
        mapper.addMapping(this) { block() }
    }

    // suspended action with optional new state *=
    suspend operator fun EntryBuilder.timesAssign(block: suspend ExecutorScope.() -> State?) {
        mapper.addMapping(this) {
            ValueDataHolder(block() as State)
        }
    }

    // suspended action only
    suspend operator fun EntryBuilder.minusAssign(block: suspend ExecutorScope.() -> Unit) {
        mapper.addMapping(this) { block(); state }
    }
}

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder>.events
    get() = filter { it.value is Event }.map { it }.toSet()

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder>.states
    get() = filter { it.value is State }.map { it }.toSet()