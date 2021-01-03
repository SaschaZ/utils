package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log2.ILogScope
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.Matcher
import dev.zieger.utils.statemachine.OnStateChanged

/**
 *
 */
@Suppress("EmptyRange", "PrmxopertyName")
val X: IntRange = 0..-1

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class EventPrevious(combo: EventCombo, range: IntRange) : Previous<AbsEventType>(combo, range),
    AbsEventType by combo.master

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class StatePrevious(combo: StateCombo, range: IntRange) : Previous<AbsStateType>(combo, range),
    AbsStateType by combo.master

open class Previous<out T : Master>(
    val combo: Combo<T>,
    private val range: IntRange
) : Combo<T> by combo {

    override val hasPrevious = true

    suspend fun condition(matchScope: IMatchScope, logScope: ILogScope, block: suspend Matcher.() -> Boolean): Boolean =
        matchScope.buildScopes().any { Matcher(it, logScope).block() }

    private fun IMatchScope.buildScopes(): List<IMatchScope> = when {
        range == X -> (0..previousChanges.size)
        range.last > previousChanges.size -> (range.first..previousChanges.size)
        else -> range
    }.map { copy(eventForIdx(it), stateForIdx(it), prevChangesForIdx(it)) }

    private fun IMatchScope.eventForIdx(idx: Int): EventCombo = when (idx) {
        0 -> eventCombo
        else -> previousChanges[idx].event
    }

    private fun IMatchScope.stateForIdx(idx: Int): StateCombo = when (idx) {
        0 -> stateCombo
        else -> previousChanges[idx].stateBefore
    }

    private fun IMatchScope.prevChangesForIdx(idx: Int): List<OnStateChanged> = when (idx) {
        0 -> previousChanges
        else -> previousChanges.takeLast(previousChanges.size - idx)
    }

    override fun toString(): String = "PE(${master.toString().removeSuffix(")")}|$range)"
}