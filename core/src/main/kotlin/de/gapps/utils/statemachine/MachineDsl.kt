@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.ConditionElement.Condition
import de.gapps.utils.statemachine.ConditionElement.Slave
import de.gapps.utils.statemachine.IConditionElement.IMaster
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave
import de.gapps.utils.statemachine.IConditionElement.UsedAs.DEFINITION


abstract class MachineDsl : IMachineEx {

    // start entry with -
    operator fun IMaster.unaryMinus(): Condition = Condition(apply { usedAs = DEFINITION })

    // link wanted items with + operator
    operator fun Condition.plus(other: IMaster): Condition =
        copy(wanted = wanted + other)

    // link unwanted items with - operator
    operator fun Condition.minus(other: IMaster): Condition =
        copy(unwanted = unwanted + other.apply { usedAs = DEFINITION })


    // apply Data with * operator
    operator fun <M : ISingle, S : ISlave> M.times(slave: S?) = also { it.slave = slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun <S : ISlave> Condition.times(slave: S?) =
        copy(start = (start as? ISingle)?.apply { this.slave = slave } ?: start)

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match against the current state, it will try to match against the third last [State] instead.
     * This works for all [ConditionElement]s.
     */
    operator fun ISingle.get(idx: Int): ISingle = apply { this.idx = idx }

    // Same as += but with more proper name
    suspend infix fun Condition.set(state: IState) = execAndSet { state }

    suspend infix fun Condition.exec(block: suspend ExecutorScope.() -> Unit) = execAndSet { block(); null }

    suspend infix fun <T : IConditionElement> Condition.execAndSet(block: suspend ExecutorScope.() -> T?) {
        mapper.addCondition(this) {
            when (val result = block()) {
                is IState -> result
                else -> null
            }
        }
    }


    /**
     * Non DSL helper method to fire an [IEvent] with optional [Slave] and suspend until it was processed by the state
     * machine.
     */
    override suspend fun fire(event: IEvent, data: ISlave?) =
        fire eventSync (event * data)

    /**
     * Non DSL helper method to add an [IEvent] with optional [Slave] to the [IEvent] processing queue and return
     * immediately.
     */
    override fun fireAndForget(event: IEvent, data: ISlave?) =
        fire event (event * data)

}