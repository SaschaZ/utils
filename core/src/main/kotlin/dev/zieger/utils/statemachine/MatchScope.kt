package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.ComboEventElement
import dev.zieger.utils.statemachine.conditionelements.ComboStateElement
import dev.zieger.utils.statemachine.conditionelements.Condition
import dev.zieger.utils.statemachine.conditionelements.Data

/**
 *
 * @property newEvent new event
 * @property currentState current state
 * @property previousChanges previous state changes of the state machine
 * @property conditions
 * @property bindings
 */
interface IMatchScope {
    val newEvent: ComboEventElement
    val currentState: ComboStateElement
    val previousChanges: List<OnStateChanged>
    val conditions: Map<Long, Condition>
    val bindings: Map<Condition, IMachineEx>

    val eventData: Data? get() = newEvent.slave as? Data
    val stateData: Data? get() = currentState.slave as? Data

    @Suppress("UNCHECKED_CAST")
    fun <D : Data> eventData() = eventData as D

    @Suppress("UNCHECKED_CAST")
    fun <D : Data> stateData(idx: Int = 0) = stateData as D

    fun applyState(state: ComboStateElement): MatchScope
}

data class MatchScope(
    override val newEvent: ComboEventElement,
    override val currentState: ComboStateElement,
    override val previousChanges: List<OnStateChanged> = emptyList(),
    override val conditions: Map<Long, Condition> = emptyMap(),
    override val bindings: Map<Condition, IMachineEx> = emptyMap()
) : IMatchScope {

    override fun applyState(state: ComboStateElement): MatchScope =
        copy(
            currentState = state,
            previousChanges = previousChanges + OnStateChanged(newEvent, this.currentState, state)
        )
}