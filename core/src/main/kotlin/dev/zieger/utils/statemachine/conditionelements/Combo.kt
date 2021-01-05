package dev.zieger.utils.statemachine.conditionelements

interface Combo<out M : Master> : Master {
    val master: M
    var slave: Slave?
    var matchMasterOnly: Boolean
}

data class EventCombo(
    override val master: AbsEvent,
    override var slave: Slave? = null,
    override var matchMasterOnly: Boolean = false
) : Combo<AbsEvent>, AbsEvent by master {
    override fun toString(): String = "EC($master|$slave|${matchMasterOnly.toString()[0]})"
}

data class StateCombo(
    override val master: AbsState,
    override var slave: Slave? = null,
    override var matchMasterOnly: Boolean = false
) : Combo<AbsState>, AbsState by master {
    override fun toString(): String = "SC($master|$slave|${matchMasterOnly.toString()[0]})"
}

data class EventGroupCombo<T : AbsEvent>(
    override val master: AbsEventGroup<T>,
    override var slave: Slave? = null,
    override var matchMasterOnly: Boolean = false
) : Combo<AbsEventGroup<T>>, AbsEventGroup<T> by master {
    override fun toString(): String = "EgC($master|$slave|${matchMasterOnly.toString()[0]})"
}

data class StateGroupCombo<T : AbsState>(
    override val master: AbsStateGroup<T>,
    override var slave: Slave? = null,
    override var matchMasterOnly: Boolean = false
) : Combo<AbsStateGroup<T>>, AbsStateGroup<T> by master {

    override fun toString(): String = "SgC($master|$slave|${matchMasterOnly.toString()[0]})"
}

val Master.combo: Combo<*>
    get() = when (this) {
        is Combo<*> -> this
        is AbsEvent -> combo
        is AbsState -> combo
        is AbsEventGroup<*> -> combo
        is AbsStateGroup<*> -> combo
        else -> throw IllegalArgumentException("Unknown Master type $this")
    }

val AbsEvent.combo: EventCombo
    get() = when (this) {
        is EventCombo -> this
        else -> EventCombo(this)
    }

val AbsEventGroup<*>.combo: EventGroupCombo<*>
    get() = when (this) {
        is EventGroupCombo<*> -> this
        else -> EventGroupCombo(this)
    }

val AbsState.combo: StateCombo
    get() = when (this) {
        is StateCombo -> this
        else -> StateCombo(this)
    }

val AbsStateGroup<*>.combo: StateGroupCombo<*>
    get() = when (this) {
        is StateGroupCombo<*> -> this
        else -> StateGroupCombo(this)
    }

val ConditionElement.master get() = (this as? Combo<*>)?.master ?: this as? Master
val ConditionElement.slave get() = (this as? Combo<*>)?.slave ?: this as? Slave
val ConditionElement.ignoreSlave get() = (this as? Combo<*>)?.matchMasterOnly == true