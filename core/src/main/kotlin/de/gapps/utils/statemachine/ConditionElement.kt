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
import kotlin.reflect.full.isSuperclassOf

interface IConditionElement {

    enum class UsedAs {
        DEFINITION,
        RUNTIME
    }

    interface IMaster : IConditionElement {

        interface ISingle : IMaster {

            interface IEvent : ISingle {
                val noLogging: Boolean
                fun OnStateChanged.fired() = Unit
            }

            interface IState : ISingle {
                fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit
            }
        }

        interface IGroup<out T : ISingle> : IMaster {
            val type: KClass<@UnsafeVariance T>

            interface IEventGroup<out T : ISingle> : IGroup<T>
            interface IStateGroup<out T : ISingle> : IGroup<T>
        }
    }

    interface ISlave : IConditionElement {

        interface IData : ISlave

        interface IType<out T : IData> : ISlave {
            val type: KClass<@UnsafeVariance T>
        }
    }

    interface IComboElement : IConditionElement {

        val master: IMaster
        var slave: ISlave?
        var idx: Int
        var usedAs: UsedAs
        val ignoreSlave: Boolean

        val event: IEvent? get() = master as? IEvent
        val state: IState? get() = master as? IState

        val hasEvent get() = event != null
        val hasState get() = state != null
    }

    interface ICondition : IConditionElement {

        val start: IComboElement
        val wanted: List<IComboElement>
        val unwanted: List<IComboElement>
        val action: (suspend ExecutorScope.() -> IComboElement?)?

        enum class ConditionType {
            STATE,
            EVENT
        }

        val type: ConditionType

        val wantedEvents: List<IComboElement>
        val wantedStates: List<IComboElement>
        val unwantedStates: List<IComboElement>
        val unwantedEvents: List<IComboElement>
        val wantedStatesAll: List<IComboElement>
        val wantedStatesAny: List<IComboElement>
        val wantedEventsAll: List<IComboElement>
        val wantedEventsAny: List<IComboElement>
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

        override fun equals(other: Any?) = this === other
        override fun hashCode(): Int = this::class.hashCode()
        override fun toString(): String = this::class.name

        sealed class Single : Master(), ISingle {

            /**,
             * All events need to implement this class.
             * @property noLogging When `true` log messages for this [Event] are not printed. Default is `false`.
             */
            abstract class Event(
                override val noLogging: Boolean = false
            ) : Single(), IEvent

            /**
             * All states need to implement this class.
             */
            abstract class State : Single(), IState
        }

        sealed class Group<out T : ISingle>(override val type: KClass<@UnsafeVariance T>) : Master(), IGroup<T> {

            abstract class EventGroup<out T : IEvent>(type: KClass<T>) :
                Group<@UnsafeVariance T>(type), IEventGroup<T>

            abstract class StateGroup<out T : IState>(type: KClass<T>) :
                Group<@UnsafeVariance T>(type), IStateGroup<T>
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
        abstract class Type<out T : IData>(override val type: KClass<@UnsafeVariance T>) : Slave(), IType<T> {

            override fun equals(other: Any?): Boolean = when (other) {
                is IType<*> -> other.type == type
                is IData -> type.isInstance(other) || type.isSuperclassOf(other::class)
                else -> false
            }

            override fun hashCode(): Int = type.hashCode()
            override fun toString(): String = type.name
        }
    }

    data class ComboElement(
        override val master: IMaster,
        override var slave: ISlave? = null,
        override var idx: Int = 0,
        override var usedAs: UsedAs = DEFINITION,
        override val ignoreSlave: Boolean = false
    ) : IComboElement {
        override fun toString() = "${this::class.name}(${when (master) {
            is IEvent -> "E"
            is IState -> "S"
            else -> "X"
        }}${usedAs.name[0]} | ${listOfNotNull(
            "master=$master",
            slave?.let { "slave=$it" },
            idx.nullWhen { it == 0 }?.let { "idx=$it" },
            ignoreSlave.nullWhen { !it }?.let { "ignoreSlave=$it" }
        ).joinToString(" | ")})"
    }

    data class Condition(
        override val wanted: List<IComboElement> = emptyList(),
        override val unwanted: List<IComboElement> = emptyList(),
        override val action: (suspend ExecutorScope.() -> IComboElement?)? = null
    ) : ICondition {

        constructor(master: IMaster) : this(listOf(master.combo))

        override val start: IComboElement get() = wanted.first()

        override val type = when {
            start.hasState -> STATE
            start.hasEvent -> EVENT
            else -> throw IllegalArgumentException("Unknown start type ${start::class}")
        }

        override val wantedEvents =
            mutableListOf(if (start.hasEvent) start else null).apply { addAll(wanted.filter { it.hasEvent }) }
                .filterNotNull()
        override val wantedStates =
            mutableListOf(if (start.hasState) start else null).apply { addAll(wanted.filter { it.hasState }) }
                .filterNotNull()

        override val unwantedEvents = unwanted.filter { it.hasEvent }
        override val unwantedStates = unwanted.filter { it.hasState }

        override val wantedStatesAll = wantedStates.filter { it.idx > 0 }
        override val wantedStatesAny = wantedStates.filter { it.idx == 0 }
        override val wantedEventsAll = wantedEvents.filter { it.idx > 0 }
        override val wantedEventsAny = wantedEvents.filter { it.idx == 0 }

        fun match(
            event: IComboElement,
            state: IComboElement,
            previousChanges: List<OnStateChanged>
        ): Boolean = Matcher.match(this, event, state, previousChanges)

        override fun toString(): String =
            "${this::class.name}(${listOfNotNull("start=$start",
                wanted.nullWhen { it.isEmpty() }?.let { "wanted=$it" },
                unwanted.nullWhen { it.isEmpty() }?.let { "unwanted=$it" }
            ).joinToString("; ")})"
    }
}

val ICondition.isStateCondition get() = type == STATE
val ICondition.isEventCondition get() = type == EVENT

val IComboElement.isDefinition get() = usedAs == DEFINITION
val IComboElement.isRuntime get() = usedAs == UsedAs.RUNTIME

val IComboElement.noLogging: Boolean get() = (master as? IEvent)?.noLogging == true

val IMaster.combo get() = ConditionElement.ComboElement(this)