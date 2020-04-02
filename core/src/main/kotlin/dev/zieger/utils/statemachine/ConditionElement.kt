@file:Suppress("MemberVisibilityCanBePrivate", "unused", "RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.misc.whenNotNull
import dev.zieger.utils.statemachine.ConditionElement.ComboElement
import dev.zieger.utils.statemachine.IConditionElement.*
import dev.zieger.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import dev.zieger.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import dev.zieger.utils.statemachine.IConditionElement.IConditionElementGroup.MatchType
import dev.zieger.utils.statemachine.IConditionElement.IConditionElementGroup.MatchType.*
import dev.zieger.utils.statemachine.IConditionElement.IMaster.IGroup
import dev.zieger.utils.statemachine.IConditionElement.IMaster.IGroup.IEventGroup
import dev.zieger.utils.statemachine.IConditionElement.IMaster.IGroup.IStateGroup
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import dev.zieger.utils.statemachine.IConditionElement.ISlave.IData
import dev.zieger.utils.statemachine.IConditionElement.ISlave.IType
import dev.zieger.utils.statemachine.IConditionElement.UsedAs.DEFINITION
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

interface IConditionElement {

    fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean

    enum class UsedAs {
        DEFINITION,
        RUNTIME
    }

    interface IMaster : IConditionElement {

        interface ISingle : IMaster {

            interface IEvent : ISingle {
                val noLogging: Boolean
                fun OnStateChanged.fired() = Unit

                override fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IEvent -> this === other
                        is IEventGroup<IEvent> -> other.type.isSuperclassOf(this::class)
                        else -> false
                    } logV { m = "#E $it => ${this@IEvent} <||> $other" }
                }
            }

            interface IState : ISingle {
                fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit

                override fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IState -> this === other
                        is IStateGroup<IState> -> other.type.isSuperclassOf(this::class)
                        else -> false
                    } logV { m = "#ST $it => ${this@IState} <||> $other" }
                }
            }
        }

        interface IGroup<out T : ISingle> : IMaster {
            val type: KClass<@UnsafeVariance T>

            interface IEventGroup<out T : IEvent> : IGroup<T> {

                override fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IEvent -> type.isInstance(other)
                        is IEventGroup<*> -> other.type == type
                        null -> false
                        else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                    } logV { m = "#EG $it => ${this@IEventGroup} <||> $other" }
                }
            }

            interface IStateGroup<out T : IState> : IGroup<T> {

                override fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IState -> type.isInstance(other)
                        is IStateGroup<*> -> other.type == type
                        null -> false
                        else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                    } logV { m = "#SG $it => ${this@IStateGroup} <||> $other" }
                }
            }
        }
    }

    interface ISlave : IConditionElement {

        interface IData : ISlave {

            override fun match(
                other: IConditionElement?,
                previousStateChanges: List<OnStateChanged>
            ): Boolean {
                return when (other) {
                    is IData -> this == other
                    is IType<*> -> other.type.isInstance(this)
                    null -> false
                    else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                } logV { m = "#D $it => ${this@IData} <||> $other" }
            }
        }

        interface IType<out T : IData> : ISlave {
            val type: KClass<@UnsafeVariance T>

            override fun match(
                other: IConditionElement?,
                previousStateChanges: List<OnStateChanged>
            ): Boolean {
                return when (other) {
                    is IData -> type.isInstance(other)
                    is IType<*> -> other.type == type
                    null -> false
                    else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                } logV { m = "#T $it => ${this@IType} <||> $other" }
            }
        }
    }

    interface IComboElement : IConditionElement {

        val master: IMaster
        var slave: ISlave?
        var idx: Int
        var usedAs: UsedAs
        val ignoreSlave: Boolean
        var exclude: Boolean

        val single get() = master as? ISingle
        val event get() = master as? IEvent
        val state get() = master as? IState
        val group get() = master as? IGroup<*>
        val eventGroup get() = master as? IEventGroup<*>
        val stateGroup get() = master as? IStateGroup<*>

        val hasSingle get() = single != null
        val hasEvent get() = event != null
        val hasState get() = state != null
        val hasGroup get() = group != null
        val hasStateGroup get() = stateGroup != null
        val hasEventGroup get() = eventGroup != null

        override fun match(
            other: IConditionElement?,
            previousStateChanges: List<OnStateChanged>
        ): Boolean {
            @Suppress("UNCHECKED_CAST")
            fun <T : IConditionElement> T.get(idx: Int): T? {
                return when (idx) {
                    0 -> this
                    else -> previousStateChanges.getOrNull(previousStateChanges.size - idx)?.let { result ->
                        when (this) {
                            is IEvent -> result.event.master as T
                            is IState -> result.stateBefore.master as T
                            is IComboElement -> when {
                                hasEvent || hasEventGroup -> ConditionElement.ComboElement(
                                    result.event.master,
                                    result.event.slave
                                ) as T
                                hasState || hasStateGroup -> ConditionElement.ComboElement(
                                    result.stateBefore.master,
                                    result.stateBefore.slave
                                ) as T
                                else -> throw IllegalArgumentException("Unsupported type ${this::class.name}")
                            }
                            else -> throw IllegalArgumentException("Unsupported type ${this::class.name}")
                        }
                    }
                }
            }

            return when (other) {
                is IComboElement -> whenNotNull(get(other.idx), other.get(idx)) { t, o ->
                    t.master.match(o.master, previousStateChanges)
                            && (t.slave == null && o.slave == null || t.ignoreSlave || o.ignoreSlave
                            || t.slave?.match(o.slave, previousStateChanges) == true)
                } ?: false
                is IInputElement -> other.match(this, previousStateChanges)
                null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            } logV {
                m = "#CE $it => ${this@IComboElement.get((other as? IComboElement)?.idx ?: 0)} <||> ${other?.get(idx)}"
            }
        }
    }

    interface IConditionElementGroup : IConditionElement {

        enum class MatchType {
            ALL,
            ANY,
            NONE
        }

        val matchType: MatchType
        val elements: MutableList<IComboElement>

        override fun match(
            other: IConditionElement?,
            previousStateChanges: List<OnStateChanged>
        ): Boolean {
            return when (other) {
                is IInputElement -> {
                    val filtered = elements.filter {
                        it.hasEvent && other.event.hasEvent
                                || it.hasState && other.state.hasState
                                || it.hasEventGroup && other.event.hasEventGroup
                                || it.hasStateGroup && other.state.hasStateGroup
                    }
                    when (matchType) {
                        ALL -> filtered.all { it.match(other, previousStateChanges) }
                        ANY -> filtered.any { it.match(other, previousStateChanges) }
                        NONE -> filtered.none { it.match(other, previousStateChanges) }
                    } logV { m = "#CG $it => ${this@IConditionElementGroup} <||> $other" }
                }
                is IComboElement -> {
                    val filtered = elements.filter {
                        it.hasEvent && other.hasEvent || it.hasState && other.hasState
                                || it.hasEventGroup && other.hasEventGroup || it.hasStateGroup && other.hasStateGroup
                    }
                    when (matchType) {
                        ALL -> filtered.all { it.match(other, previousStateChanges) }
                        ANY -> filtered.any { it.match(other, previousStateChanges) }
                        NONE -> filtered.none { it.match(other, previousStateChanges) }
                    } logV { m = "#CG $it => ${this@IConditionElementGroup} <||> $other" }
                }
                null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            }
        }
    }

    interface ICondition : IConditionElement {

        val items: List<IConditionElementGroup>
        val action: (suspend ExecutorScope.() -> IComboElement?)?

        val start: IComboElement get() = items.first().elements.first()

        val eventsAny
            get() = items.flatMap { f -> f.elements.filter { !it.exclude && it.idx == 0 && (it.hasEvent || it.hasEventGroup) } }
        val statesAny
            get() = items.flatMap { f -> f.elements.filter { !it.exclude && it.idx == 0 && (it.hasState || it.hasStateGroup) } }
        val eventsAll
            get() = items.flatMap { f -> f.elements.filter { !it.exclude && it.idx > 0 && (it.hasEvent || it.hasEventGroup) } }
        val statesAll
            get() = items.flatMap { f -> f.elements.filter { !it.exclude && it.idx > 0 && (it.hasState || it.hasStateGroup) } }
        val eventsNone
            get() = items.flatMap { f -> f.elements.filter { it.exclude && (it.hasEvent || it.hasEventGroup) } }
        val statesNone
            get() = items.flatMap { f -> f.elements.filter { it.exclude && (it.hasState || it.hasStateGroup) } }

        enum class ConditionType {
            STATE,
            EVENT
        }

        val type: ConditionType
            get() = when (start.master) {
                is IEvent,
                is IEventGroup<IEvent> -> EVENT
                is IState,
                is IStateGroup<IState> -> STATE
                else -> throw IllegalArgumentException("Unexpected first element $start")
            }

        override fun match(
            other: IConditionElement?,
            previousStateChanges: List<OnStateChanged>
        ): Boolean {
            return when (other) {
                is IInputElement -> other.match(this, previousStateChanges)
                null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            } logV { m = "#C $it => ${this@ICondition} <||> $other" }
        }
    }

    interface IInputElement : IConditionElement {

        val event: IComboElement
        val state: IComboElement

        override fun match(other: IConditionElement?, previousStateChanges: List<OnStateChanged>): Boolean {
            return when (other) {
                is ICondition -> {
                    (other.eventsAny.isEmpty() || other.eventsAny.any { match(it, previousStateChanges) })
                            && (other.eventsAll.isEmpty() || other.eventsAll.all { match(it, previousStateChanges) })
                            && (other.statesAny.isEmpty() || other.statesAny.any { match(it, previousStateChanges) })
                            && (other.statesAll.isEmpty() || other.statesAll.all { match(it, previousStateChanges) })
                            && (other.eventsNone.isEmpty() || other.eventsNone.none { match(it, previousStateChanges) })
                            && (other.statesNone.isEmpty() || other.statesNone.none { match(it, previousStateChanges) })
                }
                is IConditionElementGroup -> other.match(this, previousStateChanges)
                is IComboElement -> when {
                    other.hasEvent || other.hasEventGroup -> event.match(other, previousStateChanges)
                    other.hasState || other.hasStateGroup -> state.match(other, previousStateChanges)
                    else -> false
                }
                null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            } logV { m = "#IE $it => ${this@IInputElement} <||> $other" }
        }
    }
}


/**
 * Base class for [IMaster]s, [ISlave]s, [IComboElement] and [ICondition].
 */
sealed class ConditionElement : IConditionElement {

    override fun toString(): String = this::class.name

    /**
     * Base class for [IEvent]s and [IState]s.
     */
    sealed class Master : ConditionElement(), IMaster {

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
                Group<@UnsafeVariance T>(type),
                IEventGroup<T> {
                override fun toString(): String = type.name
            }

            abstract class StateGroup<out T : IState>(type: KClass<T>) :
                Group<@UnsafeVariance T>(type),
                IStateGroup<T> {
                override fun toString(): String = type.name
            }
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

            override fun toString(): String = type.name
        }
    }

    data class ComboElement(
        override val master: IMaster,
        override var slave: ISlave? = null,
        override var idx: Int = 0,
        override var usedAs: UsedAs = DEFINITION,
        override val ignoreSlave: Boolean = false,
        override var exclude: Boolean = false
    ) : IComboElement {
        override fun toString() = "CE($master|$slave|$idx|$ignoreSlave|$exclude|${when (master) {
            is IEvent -> "E"
            is IState -> "S"
            is IEventGroup<*> -> "Eg"
            is IStateGroup<*> -> "Sg"
            else -> "X[${master::class}]"
        }}${usedAs.name[0]})"
    }

    data class ConditionElementGroup(
        override val matchType: MatchType,
        override val elements: MutableList<IComboElement>
    ) : IConditionElementGroup {
        override fun toString(): String = "CG($matchType; $elements)"
    }

    data class Condition(
        override val items: List<IConditionElementGroup> = INITIAL_ITEMS,
        override val action: (suspend ExecutorScope.() -> IComboElement?)? = null
    ) : ICondition {

        constructor(master: IMaster) :
                this(INITIAL_ITEMS.apply { first { it.matchType == ANY }.elements.add(master.combo) })

        companion object {
            private val INITIAL_ITEMS
                get() = listOf(
                    ConditionElementGroup(ANY, ArrayList()),
                    ConditionElementGroup(ALL, ArrayList()),
                    ConditionElementGroup(NONE, ArrayList())
                )
        }

        override fun toString(): String = "C($items)"
    }

    data class InputElement(
        override val event: IComboElement,
        override val state: IComboElement
    ) : IInputElement {
        override fun toString(): String = "IE($event, $state)"
    }
}

val ICondition.isStateCondition get() = type == STATE
val ICondition.isEventCondition get() = type == EVENT

val IComboElement.isDefinition get() = usedAs == DEFINITION
val IComboElement.isRuntime get() = usedAs == UsedAs.RUNTIME

val IComboElement.noLogging: Boolean get() = (master as? IEvent)?.noLogging == true

val IMaster.combo get() = ComboElement(this)