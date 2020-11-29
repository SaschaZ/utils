package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.name
import kotlin.reflect.KClass

/**
 * Base class for [AbsEvent]s and [AbsState]s.
 */
interface Master : Definition {

    override val hasEvent get() = this is AbsEvent
    override val hasState get() = this is AbsState
    override val hasStateGroup get() = this is AbsStateGroup<*>
    override val hasEventGroup get() = this is AbsEventGroup<*>
}

interface Group<T : Single> : Master {
    val groupType: KClass<T>
}

interface Single : Master

interface AbsEventType : Master

interface AbsEvent : Single, AbsEventType {

    val noLogging: Boolean get() = false
}

/**,
 * All events need to implement this class.
 * @property noLogging When `true` log messages for this [AbsEvent] are not printed. Default is `false`.
 */
open class Event(
    override val noLogging: Boolean = false
) : AbsEvent {

    override fun toString(): String = "E(${this::class.name})"
}

interface AbsStateType : Master

interface AbsState : Single, AbsStateType

/**
 * All states need to implement this class.
 */
open class State : AbsState {

    override fun toString(): String = "S(${this::class.name})"
}

interface AbsEventGroup<T : AbsEvent> : Group<T>, AbsEventType

open class EventGroup<T : AbsEvent>(
    override val groupType: KClass<T>
) : AbsEventGroup<T> {

    override fun toString(): String = "Eg(${groupType.name})"
}

interface AbsStateGroup<T : AbsState> : Group<T>, AbsStateType

open class StateGroup<T : AbsState>(
    override val groupType: KClass<T>
) : AbsStateGroup<T> {

    override fun toString(): String = "Sg(${groupType.name})"
}