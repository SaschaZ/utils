@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.misc.nullWhen
import de.gapps.utils.statemachine.IConditionElement.*
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.IMaster.IGroup
import de.gapps.utils.statemachine.IConditionElement.IMaster.IGroup.IEventGroup
import de.gapps.utils.statemachine.IConditionElement.IMaster.IGroup.IStateGroup
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.ISlave.IType
import de.gapps.utils.statemachine.IConditionElement.UsedAs.DEFINITION
import kotlin.reflect.KClass

interface IConditionElement {

    enum class UsedAs {
        DEFINITION,
        RUNTIME
    }

    interface IMaster : IConditionElement {
        var usedAs: UsedAs

        interface ISingle : IMaster {

            var slave: ISlave?
            var idx: Int
            val ignoreSlave: Boolean

            interface IEvent : ISingle {
                val noLogging: Boolean
                fun OnStateChanged.fired() = Unit
            }

            interface IState : ISingle {
                fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit
            }
        }

        interface IGroup : IMaster {

            interface IEventGroup : IGroup
            interface IStateGroup : IGroup
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

        enum class ConditionType {
            STATE,
            EVENT
        }

        val type: ConditionType

        val wantedEvents: List<IEvent>
        val wantedStates: List<IState>
        val unwantedStates: List<IState>
        val unwantedEvents: List<IEvent>
        val wantedStatesAll: List<IState>
        val wantedStatesAny: List<IState>
        val wantedEventsAll: List<IEvent>
        val wantedEventsAny: List<IEvent>
    }

}


/**
 * Base class for [IEvent]s, [IState]s, [IData].
 */
sealed class ConditionElement : IConditionElement {

    override fun toString(): String = this::class.name

    /**
     * Base class for [IEvent]s and [IState]s.
     */
    sealed class Master : ConditionElement(), IMaster {
        override var usedAs: UsedAs = DEFINITION

        sealed class Single : Master(), ISingle {

            override fun equals(other: Any?) =
                matchClass(other) && slave == (other as? ISingle)?.slave

            override fun hashCode(): Int = this::class.hashCode() + (slave?.hashCode() ?: 0)

            override fun toString(): String = "${this::class.name}(" +
                    "${listOfNotNull("${usedAs.name[0]}", slave?.let { "slave=$it" },
                        idx.nullWhen { it == 0 }?.let { "idx=$it" },
                        if (ignoreSlave) "ignoreSlave=true" else null
                    ).joinToString("; ")})"

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
                override var usedAs: UsedAs = DEFINITION,
                override var ignoreSlave: Boolean = false,
                override val noLogging: Boolean = false
            ) : Single(), IEvent

            /**
             * All states need to implement this class.
             */
            abstract class State(
                override var slave: ISlave? = null,
                override var idx: Int = 0,
                override var usedAs: UsedAs = DEFINITION,
                override val ignoreSlave: Boolean = false
            ) : Single(), IState
        }

        sealed class Group : Master(), IGroup {

            abstract class EventGroup(override var usedAs: UsedAs = DEFINITION) : Group(), IEventGroup

            abstract class StateGroup(override var usedAs: UsedAs = DEFINITION) : Group(), IStateGroup
        }
    }

    /**
     * Type that can be combined added to [Master].
     */
    sealed class Slave : ConditionElement(), ISlave {

        /**
         * Every data needs to implement this class.
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

        override val type = when (start) {
            is IState -> STATE
            is IEvent -> EVENT
            else -> throw IllegalArgumentException("Unknown start type ${start::class}")
        }

        override val wantedEvents = wanted.filterIsInstance<IEvent>()
        override val wantedStates = wanted.filterIsInstance<IState>()

        override val unwantedEvents = unwanted.filterIsInstance<IEvent>()
        override val unwantedStates = unwanted.filterIsInstance<IState>()

        override val wantedStatesAll = wantedStates.filter { it.idx > 0 }
        override val wantedStatesAny = wantedStates.filter { it.idx == 0 }
        override val wantedEventsAll = wantedEvents.filter { it.idx > 0 }
        override val wantedEventsAny = wantedEvents.filter { it.idx == 0 }

        fun match(
            event: IEvent,
            state: IState,
            previousChanges: List<OnStateChanged>
        ): Boolean = Matcher.run { match(event, state, previousChanges) }

        override fun toString(): String =
            "${this::class.name}(${listOfNotNull("start=$start",
                wanted.nullWhen { it.isEmpty() }?.let { "wanted=$it" },
                unwanted.nullWhen { it.isEmpty() }?.let { "unwanted=$it" },
                "type=$type"
            ).joinToString("; ")}"
    }
}

val ICondition.isStateCondition get() = type == STATE
val ICondition.isEventCondition get() = type == EVENT

val IMaster.isDefinition get() = usedAs == DEFINITION
val IMaster.isRuntime get() = usedAs == UsedAs.RUNTIME