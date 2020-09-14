package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.OnStateChanged
import kotlin.reflect.KClass

/**
 * Base class for [Event]s and [State]s.
 */
interface Master : DefinitionElement {

    override val hasEvent get() = this is Event
    override val hasState get() = this is State
    override val hasStateGroup get() = this is StateGroup<*>
    override val hasEventGroup get() = this is EventGroup<*>
}

interface Group<T : Single> : Master {
    val groupType: KClass<T>
}

interface Single : Master

interface Event : Single {

    val noLogging: Boolean get() = false

    fun OnStateChanged.fired() = Unit
}

/**,
 * All events need to implement this class.
 * @property noLogging When `true` log messages for this [Event] are not printed. Default is `false`.
 */
open class EventImpl(
    override val noLogging: Boolean = false
) : Event {

    override fun toString(): String = "E(${this::class.name})"
}

interface State : Single {

    fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit
}

/**
 * All states need to implement this class.
 */
open class StateImpl : State {

    override fun toString(): String = "S(${this::class.name})"
}

interface EventGroup<T : Event> : Group<T>

open class EventGroupImpl<T : Event>(
    override val groupType: KClass<T>
) : EventGroup<T> {

    override fun toString(): String = "Eg(${groupType.name})"
}

interface StateGroup<T : State> : Group<T>

open class StateGroupImpl<T : State>(
    override val groupType: KClass<T>
) : StateGroup<T> {

    override fun toString(): String = "Sg(${groupType.name})"
}