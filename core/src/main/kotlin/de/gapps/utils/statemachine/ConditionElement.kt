@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.ConditionElement.*
import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.ConditionElement.Master.State
import de.gapps.utils.statemachine.ConditionElement.Slave.Data
import de.gapps.utils.statemachine.IConditionElement.*
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement.UsedAs
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.IMaster.*
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.ISlave.IType
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

interface IMatcher {

    val ignoreSlave: Boolean

    /**
     * Return `true` when [other] matches against this or when [other] is `null` against the [MatchScope].
     */
    fun MatchScope.match(other: IConditionElement? = null): Boolean = this@IMatcher == other
}

interface IConditionElement : IMatcher {

    interface IMaster : IConditionElement {
        interface IEvent : IMaster {
            val noLogging: Boolean
            fun OnStateChanged.fired() = Unit
        }

        interface IEventGroup : IMaster {
            override val ignoreSlave: Boolean get() = false
            override fun MatchScope.match(other: IConditionElement?): Boolean =
                other != null && this@IEventGroup::class.isSuperclassOf(other::class)
        }

        interface IState : IMaster {
            fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit
        }

        interface IStateGroup : IMaster {
            override val ignoreSlave: Boolean get() = false
            override fun MatchScope.match(other: IConditionElement?): Boolean =
                other != null && this@IStateGroup::class.isSuperclassOf(other::class)
        }
    }

    interface ISlave : IConditionElement {
        interface IData : ISlave

        interface IType<out T : IData> : ISlave {
            val type: KClass<@UnsafeVariance T>
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
        val slaves: Set<ISlave>
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
         * @property noLogging When `true` log messages for this [Event] are not printed. Default is `false`.
         */
        abstract class Event(
            override var ignoreSlave: Boolean = false,
            override val noLogging: Boolean = false
        ) : Master(), IEvent

        abstract class EventGroup : Master(), IEventGroup

        /**
         * All states need to implement this class.
         */
        abstract class State(override var ignoreSlave: Boolean = false) : Master(), IState

        abstract class StateGroup : Master(), IStateGroup
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
        abstract class Type<out T : Data>(override val type: KClass<@UnsafeVariance T>) : Slave(), IType<T> {
            override fun MatchScope.match(other: IConditionElement?): Boolean =
                (other?.let { it::class == type } == true).also { Log.w("Type.match() ${other!!::class} == $type -> $it") }
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
        override val master: IMaster,
        override val slaves: Set<ISlave> = HashSet(),
        override val idx: Int = 0,
        override var usedAs: UsedAs = UsedAs.UNDEFINED
    ) : ConditionElement(), ICombinedConditionElement {

        override fun MatchScope.match(other: IConditionElement?): Boolean =
            (other as? ICombinedConditionElement)?.let { element ->
                master.run { match(other.master) } && (ignoreSlave || other.ignoreSlave || matchesExistingSlaves(element))
            } ?: false

        private fun MatchScope.matchesExistingSlaves(other: ICombinedConditionElement) =
            slaves.size == other.slaves.size && other.slaves.sortedBy { it::class.name }.let { otherSortedSlaves ->
                slaves.sortedBy { it::class.name }.mapIndexed { i, value ->
                    value.ignoreSlave || otherSortedSlaves[i].let { it.ignoreSlave || it.run { match(value) } }
                }
            }.all { true }

        override fun toString(): String {
            val params = listOfNotNull(
                if (idx > 0) "idx=$idx" else null,
                if (usedAs != UsedAs.UNDEFINED) usedAs.name else null,
                if (slaves.isNotEmpty()) "data=$slaves" else null
            ).joinToString("; ")
            return "${master::class.name}($params)"
        }

    }

    data class Condition(
        override val start: CombinedConditionElement,
        override val wanted: Set<CombinedConditionElement> = emptySet(),
        override val unwanted: Set<CombinedConditionElement> = emptySet(),
        override val action: suspend ExecutorScope.() -> ICombinedConditionElement? = { null }
    ) : ICondition {

        override fun MatchScope.match(other: IConditionElement?): Boolean {
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
                EVENT -> start.run { match(event) }
                        && (wantedStatesAny.isEmpty() || wantedStatesAny.any { it.run { match(state) } })
                        && (wantedStatesAll.isEmpty() || wantedStatesAll.all { it.run { match(pc.state(it.idx)) } })
                        && (unwantedStates.isEmpty() || unwantedStates.all { it.run { !match(pc.state(it.idx)) } })
                STATE -> start.run { match(state) }
                        && (wantedEventsAny.isEmpty() || wantedEventsAny.any { it.run { match(event) } })
                        && (wantedEventsAll.isEmpty() || wantedEventsAll.all { it.run { match(pc.event(it.idx)) } })
                        && (unwantedEvents.isEmpty() || unwantedEvents.all { it.run { !match(pc.event(it.idx)) } })
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
fun <T : IMaster> T.holder(
    usedAs: UsedAs,
    idx: Int = 0
): CombinedConditionElement = CombinedConditionElement(this, idx = idx, usedAs = usedAs)

/**
 * Creates a new [Set] with the [ConditionElement].
 */
val <T : Any> T?.toSet get() = this?.let { setOf(it).toMutableSet() } ?: HashSet()