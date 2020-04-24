@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.ConditionElement.*
import de.gapps.utils.statemachine.ConditionElement.Master.Single.External
import de.gapps.utils.statemachine.IConditionElement.*
import de.gapps.utils.statemachine.IConditionElement.IConditionElementGroup.MatchType.*
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import kotlin.math.max


abstract class MachineDsl : IMachineEx {

    // start entry with unary +
    operator fun IMaster.unaryPlus() = Condition(this)

    // link wanted items with + operator
    operator fun Condition.plus(other: IMaster): Condition = plus(other.combo)
    operator fun Condition.plus(other: suspend MatchScope.() -> Boolean): Condition {
        items.first { it.matchType == ALL }.elements.add(External(other).combo)
        return this
    }

    operator fun Condition.plus(other: IComboElement): Condition {
        items.first { it.matchType == ANY }.elements.add(other)
        return this
    }

    // link unwanted items with - operator
    operator fun Condition.minus(other: IMaster): Condition = minus(other.combo)
    operator fun Condition.minus(other: suspend MatchScope.() -> Boolean): Condition {
        items.first { it.matchType == NONE }.elements.add(External(other).combo)
        return this
    }

    operator fun Condition.minus(other: IComboElement): Condition {
        items.first { it.matchType == NONE }.elements.add(other.apply { exclude = true })
        return this
    }


    // apply Data with * operator
    operator fun ISingle.times(slave: ISlave?) = combo.also { it.slave = slave }
    operator fun IComboElement.times(slave: ISlave?) = also { it.slave = slave }
    operator fun IPrevElement.times(slave: ISlave?) = also { it.combo.slave = slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun Condition.times(slave: ISlave?) = apply { start.also { it.slave = slave } }

    interface IPrevElement {
        val combo: IComboElement
        val range: IntRange
        val idx: Int
    }

    data class PrevElement(
        override val combo: IComboElement,
        override val range: IntRange
    ) : IPrevElement {
        override val idx: Int
            get() = max(range.first, range.last)
    }

    operator fun Condition.plus(other: IPrevElement): Condition {
        this + {
            when {
                other.range == X -> previousChanges.any {
                    InputElement(it.event, it.stateBefore).match(other.combo, previousChanges)
                }
                other.idx == 0 -> previousChanges.getOrNull(0)?.let {
                    InputElement(it.event, it.stateAfter).match(other.combo, previousChanges)
                } ?: false
                other.range.run { endInclusive - start } > 0 -> other.range.any { idx ->
                    when (idx) {
                        0 -> previousChanges.getOrNull(0)?.let {
                            InputElement(it.event, it.stateAfter).match(other.combo, previousChanges)
                        } ?: false
                        else -> previousChanges.getOrNull(idx - 1)?.let {
                            InputElement(it.event, it.stateBefore).match(other.combo, previousChanges)
                        } ?: false
                    }
                }
                else -> other.idx.let { idx ->
                    previousChanges.getOrNull(idx - 1)?.let {
                        InputElement(it.event, it.stateBefore).match(other.combo, previousChanges)
                    } ?: false
                }
            }
        }
        return this
    }

    operator fun Condition.minus(other: IPrevElement): Condition {
        this + {
            other.range.none { idx ->
                previousChanges.getOrNull(idx)?.let {
                    InputElement(it.event, it.stateBefore).match(other.combo, previousChanges)
                } ?: false
            }
        }
        return this
    }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match
     * against the current state, it will try to match against the third last [IState] instead.
     */
    operator fun IMaster.get(idx: Int): IPrevElement = combo[idx]
    operator fun IMaster.get(range: IntRange): IPrevElement = combo[range]
    operator fun IComboElement.get(idx: Int): IPrevElement = this[idx..idx]
    operator fun IComboElement.get(range: IntRange): IPrevElement = PrevElement(this, range)

    /**
     *
     */
    @Suppress("EmptyRange", "PropertyName")
    val X: IntRange = 0..-1

    /**
     *
     */
    suspend infix fun Condition.set(state: IActionResult) = execAndSet { state }

    /**
     *
     */
    suspend infix fun Condition.exec(block: suspend ExecutorScope.() -> Unit) = execAndSet { block(); null }

    /**
     *
     */
    suspend infix fun <T : IActionResult> Condition.execAndSet(block: suspend ExecutorScope.() -> T?) {
        mapper.addCondition(this) {
            when (val result = block()) {
                is IState -> result.combo
                is IComboElement -> result
                null -> null
                else -> throw IllegalArgumentException("Only IState abd IComboElement is allowed as return in an action.")
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