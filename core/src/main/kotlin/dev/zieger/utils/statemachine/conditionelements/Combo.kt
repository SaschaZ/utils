package dev.zieger.utils.statemachine.conditionelements

interface Combo<out M : Master> : Master {
    val master: M
    var slave: Slave?
    var ignoreSlave: Boolean
}

data class EventCombo(
    override val master: Event,
    override var slave: Slave? = null,
    override var ignoreSlave: Boolean = false
) : Combo<Event>, Event by master {
    override fun toString(): String = "EC($master|$slave|${ignoreSlave.toString()[0]})"
}

data class StateCombo(
    override val master: State,
    override var slave: Slave? = null,
    override var ignoreSlave: Boolean = false
) : Combo<State>, State by master {
    override fun toString(): String = "SC($master|$slave|${ignoreSlave.toString()[0]})"
}

data class EventGroupCombo<T : Event>(
    override val master: EventGroup<T>,
    override var slave: Slave? = null,
    override var ignoreSlave: Boolean = false
) : Combo<EventGroup<T>>, EventGroup<T> by master {
    override fun toString(): String = "EgC($master|$slave|${ignoreSlave.toString()[0]})"
}

data class StateGroupCombo<T : State>(
    override val master: StateGroup<T>,
    override var slave: Slave? = null,
    override var ignoreSlave: Boolean = false
) : Combo<StateGroup<T>>, StateGroup<T> by master {

    override fun toString(): String = "SgC($master|$slave|${ignoreSlave.toString()[0]})"
}

val Master.combo: Combo<*>
    get() = when (this) {
        is Combo<*> -> this
        is Event -> combo
        is State -> combo
        is EventGroup<*> -> combo
        is StateGroup<*> -> combo
        else -> throw IllegalArgumentException("Unknown Master type $this")
    }

val Event.combo: EventCombo
    get() = when (this) {
        is EventCombo -> this
        else -> EventCombo(this)
    }

val EventGroup<*>.combo: EventGroupCombo<*>
    get() = when (this) {
        is EventGroupCombo<*> -> this
        else -> EventGroupCombo(this)
    }

val State.combo: StateCombo
    get() = when (this) {
        is StateCombo -> this
        else -> StateCombo(this)
    }

val StateGroup<*>.combo: StateGroupCombo<*>
    get() = when (this) {
        is StateGroupCombo<*> -> this
        else -> StateGroupCombo(this)
    }

val ConditionElement.master get() = (this as? Combo<*>)?.master ?: this as? Master
val ConditionElement.slave get() = (this as? Combo<*>)?.slave ?: this as? Slave
val ConditionElement.ignoreSlave get() = (this as? Combo<*>)?.ignoreSlave == true