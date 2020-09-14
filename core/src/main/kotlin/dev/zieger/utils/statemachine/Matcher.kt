package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.conditionelements.*

open class Matcher(
    val matchScope: IMatchScope
) {

    val event: EventCombo get() = matchScope.eventCombo
    val state: StateCombo get() = matchScope.stateCombo

    suspend fun Any.match(): Boolean = when (this) {
        is Condition -> (all.isEmpty() || all.all { it.match() })
                && (any.isEmpty() || any.any { it.match() })
                && (none.isEmpty() || none.none { it.match() })
        is Combo<*> -> master.match() && (ignoreSlave || when (master) {
            is Event,
            is EventGroup<*> -> event.ignoreSlave || event.slave == null && slave == null || (event.slave to slave).match()
            is State,
            is StateGroup<*> -> state.ignoreSlave || state.slave == null && slave == null || (state.slave to slave).match()
            else -> throw IllegalArgumentException("Can not match $this with event $event and state $state")
        })
        is Event -> this === event || this === event.master
        is State -> this === state || this === state.master
        is EventGroup<*> -> groupType.isInstance(event) || groupType.isInstance(event.master)
        is StateGroup<*> -> groupType.isInstance(state) || groupType.isInstance(state.master)
        is PrevElement -> condition(matchScope) { combo.match() }
        is External -> condition(matchScope)
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
        Log.v("#${element::class.name} $this => $element <||> ${this@Matcher}")
    }

    override fun toString(): String = "M($event<->$state)"
}