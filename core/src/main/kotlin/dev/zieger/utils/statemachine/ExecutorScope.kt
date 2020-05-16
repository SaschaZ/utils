@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.IComboElement
import dev.zieger.utils.statemachine.conditionelements.IData
import dev.zieger.utils.statemachine.conditionelements.ISlave

data class ExecutorScope(
    val event: IComboElement,
    val state: IComboElement,
    val previousChanges: List<OnStateChanged>
) {

    @Suppress("UNCHECKED_CAST")
    fun <D : IData> eventData() = eventData as D

    @Suppress("UNCHECKED_CAST")
    fun <D : IData> stateData(idx: Int = 0) = stateData as D

    val eventData: ISlave? = event.slave
    val stateData: ISlave? = state.slave
}