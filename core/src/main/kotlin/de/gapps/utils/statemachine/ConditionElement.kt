@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.misc.nullWhen
import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.ConditionElement.Master.State
import de.gapps.utils.statemachine.ConditionElement.Slave.Data
import de.gapps.utils.statemachine.IConditionElement.*
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.IMaster.*
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.ISlave.IType
import kotlin.reflect.KClass

interface IMatcher

interface IConditionElement {

    enum class UsedAs {
        DEFINITION,
        RUNTIME,
        UNDEFINED
    }

    interface IMaster : IConditionElement {
        var slave: ISlave?
        var idx: Int
        var usedAs: UsedAs

        val ignoreSlave: Boolean

        interface IEvent : IMaster {
            val noLogging: Boolean
            fun OnStateChanged.fired() = Unit
        }

        interface IEventGroup : IMaster {
            override val ignoreSlave: Boolean get() = false
        }

        interface IState : IMaster {
            fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit

        }

        interface IStateGroup : IMaster {
            override val ignoreSlave: Boolean get() = false
        }
    }

    interface ISlave : IConditionElement {
        interface IData : ISlave

        interface IType<out T : IData> : ISlave {
            val type: KClass<@UnsafeVariance T>
        }
    }

    interface ICondition : IConditionElement {

        val start: IMaster
        val wanted: Set<IMaster>
        val unwanted: Set<IMaster>
        val action: (suspend ExecutorScope.() -> IState?)?

        val wantedEvents get() = wanted.filterIsInstance<IEvent>()
        val wantedStates get() = wanted.filterIsInstance<IState>()

        val unwantedEvents get() = unwanted.filterIsInstance<IEvent>()
        val unwantedStates get() = unwanted.filterIsInstance<IState>()

        enum class ConditionType {
            STATE,
            EVENT
        }

        val type
            get() = when (start) {
                is IState -> STATE
                is IEvent -> EVENT
                else -> throw IllegalArgumentException("Unknown start type ${start::class}")
            }
    }

}


/**
 * Base class for [Event]s, [State]s, [Data].
 */
abstract class ConditionElement : IConditionElement {

    override fun toString(): String = this::class.name

    /**
     * Base class for [Event]s and [State]s.
     */
    abstract class Master : ConditionElement(), IMaster {

        /**,
         * All events need to implement this class.
         *
         * @property ignoreSlave  Set to `true` when the data of this event should have no influence when events get mapped.
         *                       Default is `false`.
         * @property noLogging When `true` log messages for this [Event] are not printed. Default is `false`.
         */
        abstract class Event(
            override var slave: ISlave? = null,
            override var idx: Int = 0,
            override var usedAs: UsedAs = UsedAs.UNDEFINED,
            override var ignoreSlave: Boolean = false,
            override val noLogging: Boolean = false
        ) : Master(), IEvent {
            override fun toString(): String =
                "${this::class.name}(${listOfNotNull(slave?.toString()?.let { "slave=$it" },
                    idx.nullWhen { it == 0 }?.toString()?.let { "idx=$it" },
                    if (usedAs != UsedAs.UNDEFINED) "usedAs=${usedAs.name}" else null
                ).joinToString("; ")})"
        }

        abstract class EventGroup : Master(), IEventGroup {
            override var slave: ISlave? = null
            override var idx: Int = 0
            override var usedAs = UsedAs.UNDEFINED
        }

        /**
         * All states need to implement this class.
         */
        abstract class State(
            override var slave: ISlave? = null,
            override var idx: Int = 0,
            override var usedAs: UsedAs = UsedAs.UNDEFINED,
            override var ignoreSlave: Boolean = false
        ) : IMaster, IState {
            override fun toString(): String =
                "${this::class.name}(${listOfNotNull(slave?.toString()?.let { "slave=$it" },
                    idx.nullWhen { it == 0 }?.toString()?.let { "idx=$it" },
                    if (usedAs != UsedAs.UNDEFINED) "usedAs=${usedAs.name}" else null
                ).joinToString("; ")})"
        }

        abstract class StateGroup : Master(), IStateGroup {
            override var slave: ISlave? = null
            override var idx: Int = 0
            override var usedAs = UsedAs.UNDEFINED
        }
    }

    /**
     * Type that can be combined added to [Master].
     */
    abstract class Slave : ConditionElement(), ISlave {

        /**
         * Every data needs to implement this class.
         *
         * @property ignoreSlave If `true` this data does not need a match. Default is `false`.
         */
        abstract class Data : Slave(), IData

        /**
         * Every [Data] class should implement this companion.
         */
        abstract class Type<out T : Data>(override val type: KClass<@UnsafeVariance T>) : Slave(), IType<T>
    }

    data class Condition(
        override val start: IMaster,
        override val wanted: Set<IMaster> = emptySet(),
        override val unwanted: Set<IMaster> = emptySet(),
        override val action: (suspend ExecutorScope.() -> IState?)? = null
    ) : ICondition {

        fun match(
            event: IEvent,
            state: IState,
            previousChanges: List<OnStateChanged>
        ): Boolean = Matcher.run { match(event, state, previousChanges) }

        override fun toString(): String =
            "${this::class.name}(start=$start; wanted=$wanted; unwanted=$unwanted; type=$type)"
    }
}

val ICondition.isStateCondition get() = type == STATE
val ICondition.isEventCondition get() = type == EVENT

val IMaster.isDefinition get() = usedAs == UsedAs.DEFINITION
val IMaster.isRuntime get() = usedAs == UsedAs.RUNTIME