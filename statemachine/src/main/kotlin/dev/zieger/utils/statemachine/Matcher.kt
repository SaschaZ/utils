package dev.zieger.utils.statemachine

import dev.zieger.utils.log.ILogScope
import dev.zieger.utils.statemachine.conditionelements.*

open class Matcher(
    val matchScope: IMatchScope,
    logScope: ILogScope
) : ILogScope by logScope {

    val event: EventCombo get() = matchScope.eventCombo
    val state: StateCombo get() = matchScope.stateCombo

    suspend fun Any.match(): Boolean = when (this) {
        is Condition -> (all.isEmpty() || all.all { it.match() })
                && (any.isEmpty() || any.any { it.match() })
                && (none.isEmpty() || none.none { it.match() })
        is Combo<*> -> master.match() && (matchMasterOnly || when (master) {
            is AbsEvent,
            is AbsEventGroup<*> -> event.matchMasterOnly || event.slave == null && slave == null || (event.slave to slave).match()
            is AbsState,
            is AbsStateGroup<*> -> state.matchMasterOnly || state.slave == null && slave == null || (state.slave to slave).match()
            else -> throw IllegalArgumentException("Can not match $this with event $event and state $state")
        })
        is AbsEvent -> this === event || this === event.master
        is AbsState -> this === state || this === state.master
        is AbsEventGroup<*> -> groupType.isInstance(event) || groupType.isInstance(event.master)
        is AbsStateGroup<*> -> groupType.isInstance(state) || groupType.isInstance(state.master)
        is Previous<*> -> condition(matchScope, this@Matcher) { combo.match() }
        is External -> matchExternal(matchScope)
        is Pair<*, *> -> when (val s = second) {
            is Data -> when (val f = first) {
                is Data -> f == s
                is Type<*> -> f.type.isInstance(s)
                else -> false
            }
            is Type<*> -> when (val f = first) {
                is Data -> s.type.isInstance(f)
                is Type<*> -> s.type == f.type
                else -> false
            }
            else -> false
        }
        else -> throw IllegalArgumentException("Can not match $this with event $event and state $state")
    }.log(this)

    private fun Boolean.log(element: Any): Boolean = apply {
        Log.v("#${element::class.simpleName} $this => $element <||> ${this@Matcher}")
    }

    override fun toString(): String = "(E: $event | S: $state)"
}