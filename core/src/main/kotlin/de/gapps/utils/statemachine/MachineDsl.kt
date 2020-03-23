@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.ConditionElement.*
import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.ConditionElement.Master.State
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement.UsedAs.DEFINITION


abstract class MachineDsl : IMachineEx {

    // start entry with -
    operator fun Master.unaryMinus(): Condition = Condition(holder(DEFINITION))

    // link wanted items with + operator
    operator fun Condition.plus(other: Master): Condition =
        copy(wanted = wanted + other.holder(DEFINITION).toSet)

    operator fun Condition.plus(other: CombinedConditionElement): Condition =
        copy(wanted = wanted + other.toSet)

    // link unwanted items with - operator
    operator fun Condition.minus(other: Master): Condition =
        copy(unwanted = unwanted + other.holder(DEFINITION).toSet)

    operator fun Condition.minus(other: CombinedConditionElement): Condition =
        copy(unwanted = unwanted + other.toSet)


    // apply Data with * operator
    operator fun <M : Master, S : Slave> M.times(data: S?) =
        CombinedConditionElement(this, data.toSet)

    operator fun <S : Slave> CombinedConditionElement.times(data: S?) =
        copy(slaves = slaves.apply { addAll(data.toSet) })

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun <S : Slave> Condition.times(slave: S?) =
        apply { slave?.also { start.slaves.add(slave) } }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match against the current state, it will try to match against the third last [State] instead.
     * This works for all [ConditionElement]s.
     */
    operator fun Master.get(idx: Int): CombinedConditionElement = CombinedConditionElement(this)[idx]
    operator fun CombinedConditionElement.get(idx: Int): CombinedConditionElement = copy(idx = idx)

    // define action and/or new state with assign operators:

    // action wih new state +=
    suspend operator fun Condition.plusAssign(state: State) {
        this += CombinedConditionElement(state)
    }

    // Same as += but with more proper name
    suspend infix fun Condition.set(state: State) {
        this += state
    }

    // action with new state and data +=
    suspend operator fun Condition.plusAssign(state: CombinedConditionElement) {
        this += { state }
    }

    suspend infix fun Condition.set(state: CombinedConditionElement) {
        this += state
    }

    // with optional new state and data +=
    suspend operator fun <T : ConditionElement> Condition.plusAssign(block: suspend ExecutorScope.() -> T?) {
        this execAndSet block
    }

    suspend infix fun Condition.exec(block: suspend ExecutorScope.() -> Unit) {
        mapper.addCondition(this) {
            block()
            null
        }
    }

    suspend infix fun <T : ConditionElement> Condition.execAndSet(block: suspend ExecutorScope.() -> T?) {
        mapper.addCondition(this) {
            when (val result = block()) {
                is State -> {
                    CombinedConditionElement(result)
                }
                is CombinedConditionElement -> {
                    result
                }
                else -> null
            }
        }
    }

    // action with optional new state *=
    suspend operator fun Condition.timesAssign(block: suspend ExecutorScope.() -> State?) {
        mapper.addCondition(this) {
            CombinedConditionElement(block() as State)
        }
    }

    // action only
    suspend operator fun Condition.minusAssign(block: suspend ExecutorScope.() -> Unit) {
        mapper.addCondition(this) { block(); state }
    }


    /**
     * Non DSL helper method to fire an [Event] with optional [Slave] and suspend until it was processed by the state
     * machine.
     */
    override suspend fun fire(event: Event, data: Slave?) =
        fire eventSync (event * data)

    /**
     * Non DSL helper method to add an [Event] with optional [Slave] to the [Event] processing queue and return
     * immediately.
     */
    override fun fireAndForget(event: Event, data: Slave?) =
        fire event (event * data)

}

@Suppress("UNCHECKED_CAST")
private val Set<CombinedConditionElement>.events
    get() = filter { it.master is Event }.map { it }.toSet()

@Suppress("UNCHECKED_CAST")
private val Set<CombinedConditionElement>.states
    get() = filter { it.master is State }.map { it }.toSet()