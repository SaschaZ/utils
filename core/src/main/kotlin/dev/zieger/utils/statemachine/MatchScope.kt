package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.IComboElement
import dev.zieger.utils.statemachine.conditionelements.ICondition
import dev.zieger.utils.statemachine.conditionelements.IData
import dev.zieger.utils.statemachine.conditionelements.ISlave

/**
 *
 * @property newEvent new event
 * @property currentState current state
 * @property previousChanges previous state changes of the state machine
 * @property conditions
 * @property bindings
 */
interface IMatchScope {
    val newEvent: IComboElement
    val currentState: IComboElement
    val previousChanges: List<OnStateChanged>
    val conditions: Map<Long, ICondition>
    val bindings: Map<ICondition, IMachineEx>

    val eventData: ISlave? get() = newEvent.slave
    val stateData: ISlave? get() = currentState.slave

    @Suppress("UNCHECKED_CAST")
    fun <D : IData> eventData() = eventData as D

    @Suppress("UNCHECKED_CAST")
    fun <D : IData> stateData(idx: Int = 0) = stateData as D

    fun applyState(state: IComboElement): IMatchScope
}

data class MatchScope(
    override val newEvent: IComboElement,
    override val currentState: IComboElement,
    override val previousChanges: List<OnStateChanged> = emptyList(),
    override val conditions: Map<Long, ICondition> = emptyMap(),
    override val bindings: Map<ICondition, IMachineEx> = emptyMap()
) : IMatchScope {

    override fun applyState(state: IComboElement): IMatchScope =
        copy(
            currentState = state,
            previousChanges = previousChanges + OnStateChanged(newEvent, this.currentState, state)
        )
}