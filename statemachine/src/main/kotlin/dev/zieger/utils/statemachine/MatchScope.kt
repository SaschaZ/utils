package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*

/**
 *
 * @property eventCombo new event
 * @property stateCombo current state
 * @property previousChanges previous state changes of the state machine
 * @property conditions
 * @property bindings
 */
interface IMatchScope {
    val eventCombo: EventCombo
    val stateCombo: StateCombo
    val previousChanges: List<OnStateChanged>
    val conditions: List<Condition>
    val bindings: Map<Condition, IMachineEx>

    val event: AbsEvent get() = eventCombo.master
    val state: AbsState get() = stateCombo.master
    val eventData: Data? get() = eventCombo.slave as? Data
    val stateData: Data? get() = stateCombo.slave as? Data

    fun applyState(state: StateCombo): IMatchScope

    fun copy(
        event: EventCombo = this.eventCombo,
        state: StateCombo = this.stateCombo,
        previousChanges: List<OnStateChanged> = this.previousChanges,
        conditions: List<Condition> = this.conditions,
        bindings: Map<Condition, IMachineEx> = this.bindings
    ): IMatchScope
}

inline operator fun <reified T : Data> Data?.invoke(): T = this as T

inline fun <reified T : Data> IMatchScope.eventData(): T = eventCombo.slave as T
inline fun <reified T : Data> IMatchScope.stateData(): T = stateCombo.slave as T

class MatchScope(
    override val eventCombo: EventCombo,
    override val stateCombo: StateCombo,
    override val previousChanges: List<OnStateChanged> = emptyList(),
    override val conditions: List<Condition> = emptyList(),
    override val bindings: Map<Condition, IMachineEx> = emptyMap()
) : IMatchScope {

    override fun applyState(state: StateCombo): IMatchScope =
        copy(
            state = state,
            previousChanges = previousChanges + OnStateChanged(eventCombo, this.stateCombo, state)
        )

    override fun copy(
        event: EventCombo,
        state: StateCombo,
        previousChanges: List<OnStateChanged>,
        conditions: List<Condition>,
        bindings: Map<Condition, IMachineEx>
    ): IMatchScope = MatchScope(event, state, previousChanges, conditions, bindings)
}