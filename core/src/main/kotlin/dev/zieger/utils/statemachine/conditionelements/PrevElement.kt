package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.Matcher
import dev.zieger.utils.statemachine.OnStateChanged

/**
 *
 */
@Suppress("EmptyRange", "PropertyName")
val X: IntRange = 0..-1

data class PrevElement(
    val combo: Combo<Master>,
    val range: IntRange
) : DefinitionElement {

    override val hasEvent = combo.master is Event
    override val hasState = combo.master is State
    override val hasStateGroup = combo.master is StateGroup<*>
    override val hasEventGroup = combo.master is EventGroup<*>
    override val hasPrevElement = true

    suspend fun condition(matchScope: IMatchScope, block: suspend Matcher.() -> Boolean): Boolean =
        matchScope.buildScopes().any { Matcher(it).block() }

    private fun IMatchScope.buildScopes(): List<IMatchScope> = when (range) {
        X -> (0..previousChanges.size)
        else -> if (range.last > previousChanges.size) (0..previousChanges.size) else range
    }.map { copy(eventForIdx(it), stateForIdx(it), prevChangesForIdx(it)) }

    private fun IMatchScope.eventForIdx(idx: Int): EventCombo = when (idx) {
        0 -> eventCombo
        else -> previousChanges[idx - 1].event
    }

    private fun IMatchScope.stateForIdx(idx: Int): StateCombo = when (idx) {
        0 -> stateCombo
        else -> previousChanges[idx - 1].stateBefore
    }

    private fun IMatchScope.prevChangesForIdx(idx: Int): List<OnStateChanged> = when (idx) {
        0 -> previousChanges
        else -> previousChanges.takeLast(previousChanges.size - idx)
    }

    override fun toString(): String = "PE(${master.toString().removeSuffix(")")}|$range)"
}