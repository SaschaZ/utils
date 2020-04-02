@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.ConditionElement.Condition
import dev.zieger.utils.statemachine.ConditionElement.Slave
import dev.zieger.utils.statemachine.IConditionElement.*
import dev.zieger.utils.statemachine.IConditionElement.IConditionElementGroup.MatchType.*
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IState


abstract class MachineDsl : IMachineEx {

    infix fun on(master: IMaster): Condition = +master

    // start entry with unary +
    operator fun IMaster.unaryPlus() = Condition(this)

    // link wanted items with + operator
    operator fun Condition.plus(other: IMaster): Condition = plus(other.combo)
    operator fun Condition.plus(other: IComboElement): Condition {
        items.first { it.matchType == if (other.idx > 0) ALL else ANY }.elements.add(other)
        return this
    }

    // link unwanted items with - operator
    operator fun Condition.minus(other: IMaster): Condition = minus(other.combo)
    operator fun Condition.minus(other: IComboElement): Condition {
        items.first { it.matchType == NONE }.elements.add(other.apply { exclude = true })
        return this
    }


    // apply Data with * operator
    operator fun ISingle.times(slave: ISlave?) = combo.also { it.slave = slave }
    operator fun IComboElement.times(slave: ISlave?) = also { it.slave = slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun Condition.times(slave: ISlave?) = apply { start.also { it.slave = slave } }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match
     * against the current state, it will try to match against the third last [IState] instead.
     * This works for all [ConditionElement]s.
     */
    operator fun IMaster.get(idx: Int): IComboElement = combo[idx]
    operator fun IComboElement.get(idx: Int): IComboElement = apply { this.idx = idx }

    // Same as += but with more proper name
    suspend infix fun Condition.set(state: IState) = execAndSet { state }
    suspend infix fun Condition.set(state: IComboElement) = execAndSet { state }

    suspend infix fun Condition.exec(block: suspend ExecutorScope.() -> Unit) = execAndSet { block(); null }

    suspend infix fun <T : IConditionElement> Condition.execAndSet(block: suspend ExecutorScope.() -> T?) {
        mapper.addCondition(this) {
            when (val result = block()) {
                is IState -> result.combo
                is IComboElement -> result
                else -> null
            }
        }
    }


    /**
     * Non DSL helper method to fire an [IEvent] with optional [Slave] and suspend until it was processed by the state
     * machine.
     */
    override suspend fun fire(combo: IComboElement) =
        fire eventSync combo

    /**
     * Non DSL helper method to add an [IEvent] with optional [Slave] to the [IEvent] processing queue and return
     * immediately.
     */
    override fun fireAndForget(combo: IComboElement) =
        fire event combo

}