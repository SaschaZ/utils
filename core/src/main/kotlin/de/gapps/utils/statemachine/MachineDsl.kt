@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.BaseType.*
import de.gapps.utils.statemachine.BaseType.Primary.*

data class ConditionBuilder(
    val events: MutableSet<ValueDataHolder>,
    val states: MutableSet<ValueDataHolder>,
    val isStateCondition: Boolean
)

abstract class MachineDsl : IMachineEx {

    // start entry with -
    operator fun BaseType.unaryMinus(): ConditionBuilder = ConditionBuilder(
        (this as? Event)?.let { mutableSetOf(ValueDataHolder(it)) } ?: HashSet(),
        (this as? State)?.let { mutableSetOf(ValueDataHolder(it)) } ?: HashSet(),
        this is State
    )


    // link items with + operator
    operator fun ConditionBuilder.plus(other: BaseType): ConditionBuilder {
        when (other) {
            is State -> states.add(ValueDataHolder(other))
            is Event -> events.add(ValueDataHolder(other))
        }
        return this
    }

    operator fun ConditionBuilder.plus(other: ValueDataHolder): ConditionBuilder {
        when (other.value) {
            is State -> states.add(other)
            is Event -> events.add(other)
        }
        return this
    }

    operator fun ConditionBuilder.minus(other: BaseType): ConditionBuilder {
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
    operator fun Primary.times(data: Data?) = ValueDataHolder(this, data.toSet)

    operator fun ValueDataHolder.times(data: Data?) = apply { this.data = data.toSet }

    operator fun ConditionBuilder.times(data: Data?) = apply {
        (events + states).map {
            it.apply { this.data = data.toSet }
        }
    }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match against the current state, it will try to match against the third last [State] instead.
     * This works for all [BaseType]s.
     */
    operator fun Primary.get(idx: Int): ValueDataHolder = ValueDataHolder(this)[idx]
    operator fun <T: ValueDataHolder> T.get(idx: Int): T = apply { previousIdx = idx }

    // define action and/or new state with assign operators:

    // action wih new state +=
    suspend operator fun ConditionBuilder.plusAssign(state: State) {
        this += ValueDataHolder(state)
    }

    // Same as += but with more proper name
    suspend infix fun ConditionBuilder.set(state: State) {
        this += state
    }

    // action with new state and data +=
    suspend operator fun ConditionBuilder.plusAssign(state: ValueDataHolder) {
        this += { state }
    }

    suspend infix fun ConditionBuilder.set(state: ValueDataHolder) {
        this += state
    }

    // with optional new state and data +=
    suspend operator fun <T : BaseType> ConditionBuilder.plusAssign(block: suspend ExecutorScope.() -> T?) {
        this execAndSet block
    }

    suspend infix fun ConditionBuilder.exec(block: suspend ExecutorScope.() -> Unit) {
        mapper.addMapping(this) {
            block()
            null
        }
    }

    suspend infix fun <T : BaseType> ConditionBuilder.execAndSet(block: suspend ExecutorScope.() -> T?) {
        mapper.addMapping(this) {
            when (val result = block()) {
                is State -> {
                    ValueDataHolder(result)
                }
                is ValueDataHolder -> {
                    result
                }
                else -> throw IllegalArgumentException("Unknown type ${result?.let { it::class.name }}")
            }
        }
    }

    // action with optional new state *=
    suspend operator fun ConditionBuilder.timesAssign(block: suspend ExecutorScope.() -> State?) {
        mapper.addMapping(this) {
            ValueDataHolder(block() as State)
        }
    }

    // action only
    suspend operator fun ConditionBuilder.minusAssign(block: suspend ExecutorScope.() -> Unit) {
        mapper.addMapping(this) { block(); state }
    }


    /**
     * Non DSL helper method to fire an [Event] with optional [Data] and suspend until it was processed by the state
     * machine.
     */
    override suspend fun fire(event: Event, data: Data?) =
        fire eventSync (event * data)

    /**
     * Non DSL helper method to add an [Event] with optional [Data] to the [Event] processing queue and return
     * immediately.
     */
    override fun fireAndForget(event: Event, data: Data?) =
        fire event (event * data)

}

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder>.events
    get() = filter { it.value is Event }.map { it }.toSet()

@Suppress("UNCHECKED_CAST")
private val Set<ValueDataHolder>.states
    get() = filter { it.value is State }.map { it }.toSet()