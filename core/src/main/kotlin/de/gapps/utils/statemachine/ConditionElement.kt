@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.ConditionElement.*
import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.ConditionElement.Master.State
import de.gapps.utils.statemachine.ConditionElement.Slave.Data
import de.gapps.utils.statemachine.IConditionElement.*
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement.UsedAs
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.IMaster.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.ISlave.IType

interface IConditionElement {
    val ignoreSlave: Boolean

    interface IMaster : IConditionElement {
        interface IEvent : IMaster {
            fun OnStateChanged.fired() = Unit
        }

        interface IState : IMaster {
            fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit
        }
    }

    interface ISlave : IConditionElement {
        interface IData : ISlave
        interface IType : ISlave {
            override val ignoreSlave: Boolean get() = false
        }
    }

    interface ICombinedConditionElement : IConditionElement {

        enum class UsedAs {
            DEFINITION,
            RUNTIME,
            UNDEFINED
        }

        val master: IMaster
        val slaves: MutableSet<ISlave>
        var usedAs: UsedAs
        val idx: Int

        /**
         * `true` when the provided [ConditionElement] is a [State].
         */
        val hasState get() = master is State

        /**
         * `true` when the provided [ConditionElement] is an [Event].
         */
        val hasEvent get() = master is Event

        /**
         * Returns an event as the provided type [T]. (unsafe)
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Event> event() = master as T

        /**
         * Returns a state as the provided type [T]. (unsafe)
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : State> state() = master as T

        /**
         * `true` when the [Slave] of a provided [Event] should be ignored when matching.
         */
        override val ignoreSlave get() = master.ignoreSlave

    }

    interface ICondition : IConditionElement {

        val start: ICombinedConditionElement
        val wanted: Set<ICombinedConditionElement>
        val unwanted: Set<ICombinedConditionElement>
        val action: suspend ExecutorScope.() -> ICombinedConditionElement?

        override val ignoreSlave: Boolean
            get() = false

        val wantedEvents get() = wanted.filter { it.hasEvent }
        val wantedStates get() = wanted.filter { it.hasState }

        val unwantedEvents get() = unwanted.filter { it.hasEvent }
        val unwantedStates get() = unwanted.filter { it.hasState }

        enum class ConditionType {
            STATE,
            EVENT
        }

        val type
            get() = when {
                start.hasState -> STATE
                start.hasEvent -> EVENT
                else -> throw IllegalArgumentException("Unknown start type ${start::class}")
            }

        fun MatchScope.match(): Boolean
    }

}

/**
 * Returns [Slave] of the provided type [T].
 *
 * @param idx Is used when there is more than one [Slave] instance of the provided type [T].
 *            Default is `0`.
 */
inline fun <reified T : Slave> ICombinedConditionElement.data(idx: Int = 0): T =
    slaves.toList().filterIsInstance<T>()[idx]


/**
 * Base class for [Event]s, [State]s, [Data] and [CombinedConditionElement].
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
         */
        abstract class Event(override var ignoreSlave: Boolean = false) : Master(), IEvent

        /**
         * All states need to implement this class.
         */
        abstract class State(override var ignoreSlave: Boolean = false) : Master(), IState
    }

    /**
     * Type that can be combined with [Master] in a [CombinedConditionElement].
     */
    abstract class Slave : ConditionElement(), ISlave {

        /**
         * Every data needs to implement this class.
         *
         * @property ignoreSlave If `true` this data does not need a match. Default is `false`.
         */
        abstract class Data(
            override var ignoreSlave: Boolean = false
        ) : Slave(), IData

        /**
         * Every [Data] class should implement this companion.
         */
        abstract class Type : Slave(), IType {
            override fun equals(other: Any?): Boolean = this::class.isInstance(other)
            override fun hashCode(): Int = this::class.qualifiedName.hashCode()
        }
    }


    /**
     * Container class for [Event]s or [State]s and their attached [Slave].
     *
     * @property master [Event] or [State].
     * @property slaves [Set] of [Slave] that is attached to the provided [ConditionElement].
     * @property idx Index to match against. 0 is the current item. 1 the previous item and so on..
     *                       Default is 0.
     * @property usedAs
     */
    data class CombinedConditionElement(
        override val master: Master,
        override val slaves: MutableSet<ISlave> = HashSet(),
        override val idx: Int = 0,
        override var usedAs: UsedAs = UsedAs.UNDEFINED
    ) : ConditionElement(), ICombinedConditionElement {

        override fun equals(other: Any?): Boolean = (other as? CombinedConditionElement)?.let { element ->
            matchesEmptySlaves(element)
                    || matchesExistingSlaves(element)
        } ?: false

        private fun matchesEmptySlaves(other: ICombinedConditionElement) =
            master == other.master && (ignoreSlave || other.ignoreSlave || (isRuntime && slaves.isEmpty()))

        private fun matchesExistingSlaves(other: ICombinedConditionElement) =
            master == other.master && (slaves.size == other.slaves.size
                    && other.slaves.sortedBy { it::class.name }.let { otherSortedSlaves ->
                slaves.sortedBy { it::class.name }.mapIndexed { i, value ->
                    value.ignoreSlave || otherSortedSlaves[i].let { it.ignoreSlave || it == value }
                }
            }.all { true })

        override fun hashCode(): Int =
            master.hashCode() + if (ignoreSlave) 0 else slaves.sumBy { if (it.ignoreSlave) 0 else it.hashCode() }

        override fun toString(): String = "${master::class.name}(previousIdx=$idx; data=$slaves)"

    }

    data class Condition(
        override val start: ICombinedConditionElement,
        override val wanted: Set<ICombinedConditionElement> = emptySet(),
        override val unwanted: Set<ICombinedConditionElement> = emptySet(),
        override val action: suspend ExecutorScope.() -> ICombinedConditionElement? = { null }
    ) : ICondition {

        override fun MatchScope.match(): Boolean {
            val pc = previousChanges.toList()

            fun List<OnStateChanged>.event(idx: Int = 0): ICombinedConditionElement? = when (idx) {
                0 -> event
                else -> pc.getOrNull(pc.size - idx)?.event
            }

            fun List<OnStateChanged>.state(idx: Int = 0): ICombinedConditionElement? = when (idx) {
                0 -> state
                else -> pc.getOrNull(pc.size - idx)?.stateBefore
            }

            val wantedStatesAll = wantedStates.filter { it.idx > 0 }
            val wantedStatesAny = wantedStates.filter { it.idx == 0 }
            val wantedEventsAll = wantedEvents.filter { it.idx > 0 }
            val wantedEventsAny = wantedEvents.filter { it.idx == 0 }

            return when (type) {
                EVENT -> start == event
                        && (wantedStatesAny.isEmpty() || wantedStatesAny.any { it == state })
                        && (wantedStatesAll.isEmpty() || wantedStatesAll.all { it == pc.state(it.idx) })
                        && (unwantedStates.isEmpty() || unwantedStates.all { it != pc.state(it.idx) })
                STATE -> start == state
                        && (wantedEventsAny.isEmpty() || wantedEventsAny.any { it == event })
                        && (wantedEventsAll.isEmpty() || wantedEventsAll.all { it == pc.event(it.idx) })
                        && (unwantedEvents.isEmpty() || unwantedEvents.all { it != pc.event(it.idx) })
            }
        }
    }
}

val Condition.isStateCondition get() = type == STATE
val Condition.isEventCondition get() = type == EVENT

val CombinedConditionElement.isDefinition get() = usedAs == UsedAs.DEFINITION
val CombinedConditionElement.isRuntime get() = usedAs == UsedAs.RUNTIME

/**
 * Creates a new [CombinedConditionElement] instance with the [ConditionElement].
 */
fun <T : Master> T.holder(
    usedAs: UsedAs,
    idx: Int = 0
): ICombinedConditionElement = CombinedConditionElement(this, idx = idx, usedAs = usedAs)

/**
 * Creates a new [Set] with the [ConditionElement].
 */
val <T : Any> T?.toSet get() = this?.let { setOf(it).toMutableSet() } ?: HashSet()