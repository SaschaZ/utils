package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.ILogScope
import dev.zieger.utils.misc.min
import dev.zieger.utils.misc.whenNotNull
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.Matcher
import dev.zieger.utils.statemachine.OnStateChanged

/**
 * Defines a range that will match against all previous state changes.
 */
@Suppress("EmptyRange", "PrmxopertyName")
val X: IntRange = 0..-1

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class EventPrevious(combo: EventCombo, range: IntRange) : Previous<AbsEventType>(combo, range),
    AbsEventType by combo.master

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class StatePrevious(combo: StateCombo, range: IntRange) : Previous<AbsStateType>(combo, range),
    AbsStateType by combo.master

sealed class Previous<out T : Master>(
    val combo: Combo<T>,
    private val range: IntRange
) : Master {

    override val hasPrevious = true

    suspend fun condition(matchScope: IMatchScope, logScope: ILogScope, block: suspend Matcher.() -> Boolean): Boolean =
        matchScope.buildScopes().any { Matcher(it, logScope).block() }

    private fun IMatchScope.buildScopes(): List<IMatchScope> = when (range) {
        X -> (0..previousChanges.size)
        else -> (range.first..min(range.last, previousChanges.size))
    }.mapNotNull { whenNotNull(eventForIdx(it), stateForIdx(it)) { e, s -> copy(e, s, prevChangesForIdx(it)) } }

    private fun IMatchScope.eventForIdx(idx: Int): EventCombo? = when (idx) {
        0 -> eventCombo
        else -> previousChanges.getOrNull(idx - 1)?.event
    }

    private fun IMatchScope.stateForIdx(idx: Int): StateCombo? = when (idx) {
        0 -> stateCombo
        else -> previousChanges.getOrNull(idx - 1)?.stateBefore
    }

    private fun IMatchScope.prevChangesForIdx(idx: Int): List<OnStateChanged> = when {
        idx == 0 -> previousChanges
        previousChanges.lastIndex - idx in 0..previousChanges.lastIndex ->
            previousChanges.takeLast(previousChanges.lastIndex - idx)
        else -> emptyList()
    }

    override fun toString(): String = "PE(${combo.toString().removeSuffix(")")}|$range)"
}