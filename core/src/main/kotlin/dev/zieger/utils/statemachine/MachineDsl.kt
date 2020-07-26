@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.conditionelements.IConditionElementGroup.MatchType.*
import kotlin.math.max


abstract class MachineDsl : IMachineEx {

    protected val mapper: IMachineExMapper = MachineExMapper()

    // start entry with unary +
    operator fun IMaster.unaryPlus() = Condition(this)

    // link wanted items with + operator
    operator fun Condition.plus(other: IMaster): Condition = plus(other.combo)
    operator fun Condition.plus(other: suspend IMatchScope.() -> Boolean): Condition {
        items.first { it.matchType == ALL }.elements.add(External(other).combo)
        return this
    }

    operator fun Condition.plus(other: IComboElement): Condition {
        items.first { it.matchType == ANY }.elements.add(other)
        return this
    }

    // link unwanted items with - operator
    operator fun Condition.minus(other: IMaster): Condition = minus(other.combo)
    operator fun Condition.minus(other: suspend IMatchScope.() -> Boolean): Condition {
        items.first { it.matchType == NONE }.elements.add(External(other).combo)
        return this
    }

    operator fun Condition.minus(other: IComboElement): Condition {
        items.first { it.matchType == NONE }.elements.add(other.apply { exclude = true })
        return this
    }


    // apply Data with * operator
    operator fun ISingle.times(slave: ISlave?) = combo * slave
    operator fun IComboElement.times(slave: ISlave?) = also { it.slave = slave }
    operator fun IPrevElement.times(slave: ISlave?) = also { it.combo * slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun Condition.times(slave: ISlave?) = apply { start * slave }

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

    operator fun Condition.plus(other: IPrevElement): Condition = let { it + { matchPrev(other) } }

    operator fun Condition.minus(other: IPrevElement): Condition = let { it + { !matchPrev(other) } }

    private suspend fun List<OnStateChanged>.stateChanged(
        idx: Int,
        block: suspend InputElement.() -> Boolean
    ): Boolean {
        return when (idx) {
            0 -> getOrNull(0)?.let {
                InputElement(it.event, it.stateAfter).block()
            } ?: false
            else -> getOrNull(idx - 1)?.let {
                InputElement(it.event, it.stateBefore).block()
            } ?: false
        }
    }

    private suspend fun IMatchScope.matchPrev(other: IPrevElement): Boolean {
        return when {
            other.range == X ->
                previousChanges.any {
                    InputElement(it.event, it.stateBefore).run { match(other.combo) }
                }
            other.range.run { endInclusive - start } > 0 ->
                other.range.any { idx ->
                    previousChanges.stateChanged(idx) { match(other.combo) }
                }
            else ->
                previousChanges.stateChanged(other.idx) { match(other.combo) }
        }
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
     * Binds the [Condition] to the specified [IMachineEx].
     * A bound [IMachineEx] will process all events as long as the condition matches.
     */
    infix fun Condition.bind(machine: IMachineEx) = mapper.addBinding(this, machine)


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
    suspend infix fun Condition.exec(block: suspend IMatchScope.() -> Unit) = execAndSet { block(); null }

    /**
     *
     */
    suspend infix fun <T : IActionResult> Condition.execAndSet(block: suspend IMatchScope.() -> T?) {
        mapper.addCondition(this) {
            when (val result = block()) {
                is IState -> result.combo
                is IComboElement -> result
                null -> null
                else -> throw IllegalArgumentException("Only IState abd IComboElement is allowed as return in an action.")
            }
        }
    }
}