@file:Suppress("MemberVisibilityCanBePrivate", "unused", "RemoveCurlyBracesFromTemplate")

package de.gapps.utils.statemachine

import de.gapps.utils.log.LogFilter.Companion.GENERIC
import de.gapps.utils.log.logV
import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.IConditionElement.*
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EXTERNAL
import de.gapps.utils.statemachine.IConditionElement.IConditionElementGroup.MatchType.*
import de.gapps.utils.statemachine.IConditionElement.IMaster.IGroup
import de.gapps.utils.statemachine.IConditionElement.IMaster.IGroup.IEventGroup
import de.gapps.utils.statemachine.IConditionElement.IMaster.IGroup.IStateGroup
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.*
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.ISlave.IType
import de.gapps.utils.statemachine.IConditionElement.UsedAs.DEFINITION
import de.gapps.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

val IConditionElement?.disableLogging
    get() = (this as? IEvent)?.noLogging == true

interface IConditionElement {

    suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean

    enum class UsedAs {
        DEFINITION,
        RUNTIME
    }

    interface IActionResult

    interface IMaster : IConditionElement {

        interface ISingle : IMaster {

            interface IEvent : ISingle {
                val noLogging: Boolean
                fun OnStateChanged.fired() = Unit

                override suspend fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IEvent -> this === other
                        is IEventGroup<IEvent> -> other.type.isSuperclassOf(this::class)
                        else -> false
                    } logV {
                        f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                        m = "#E $it => ${this@IEvent} <||> $other"
                    }
                }
            }

            interface IState : ISingle, IActionResult {
                fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit

                override suspend fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IState -> this === other
                        is IStateGroup<IState> -> other.type.isSuperclassOf(this::class)
                        else -> false
                    } logV {
                        f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                        m = "#ST $it => ${this@IState} <||> $other"
                    }
                }
            }

            interface IExternal : ISingle {

                val condition: suspend MatchScope.() -> Boolean

                override suspend fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean =
                    MatchScope(previousStateChanges).condition() logV {
                        f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                        m = "#EX $it => ${this@IExternal} <||> $other"
                    }
            }
        }

        interface IGroup<out T : ISingle> : IMaster {
            val type: KClass<@UnsafeVariance T>

            interface IEventGroup<out T : IEvent> : IGroup<T> {

                override suspend fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IEvent -> type.isInstance(other)
                        is IEventGroup<*> -> other.type == type
                        null -> false
                        else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                    } logV {
                        f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                        m = "#EG $it => ${this@IEventGroup} <||> $other"
                    }
                }
            }

            interface IStateGroup<out T : IState> : IGroup<T> {

                override suspend fun match(
                    other: IConditionElement?,
                    previousStateChanges: List<OnStateChanged>
                ): Boolean {
                    return when (other) {
                        is IState -> type.isInstance(other)
                        is IStateGroup<*> -> other.type == type
                        null -> false
                        else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                    } logV {
                        f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                        m = "#SG $it => ${this@IStateGroup} <||> $other"
                    }
                }
            }
        }
    }

    interface ISlave : IConditionElement {

        interface IData : ISlave {

            override suspend fun match(
                other: IConditionElement?,
                previousStateChanges: List<OnStateChanged>
            ): Boolean {
                return when (other) {
                    is IData -> this == other
                    is IType<*> -> other.type.isInstance(this)
                    null -> false
                    else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                } logV {
                    f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                    m = "#D $it => ${this@IData} <||> $other"
                }
            }
        }

        interface IType<out T : IData> : ISlave {
            val type: KClass<@UnsafeVariance T>

            override suspend fun match(
                other: IConditionElement?,
                previousStateChanges: List<OnStateChanged>
            ): Boolean {
                return when (other) {
                    is IData -> type.isInstance(other)
                    is IType<*> -> other.type == type
                    null -> false
                    else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
                } logV {
                    f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                    m = "#T $it => ${this@IType} <||> $other"
                }
            }
        }
    }

    interface IComboElement : IConditionElement, IActionResult {

        val master: IMaster
        var slave: ISlave?
        var usedAs: UsedAs
        val ignoreSlave: Boolean
        var exclude: Boolean

        val single get() = master as? ISingle
        val event get() = master as? IEvent
        val state get() = master as? IState
        val group get() = master as? IGroup<*>
        val eventGroup get() = master as? IEventGroup<*>
        val stateGroup get() = master as? IStateGroup<*>
        val external get() = single as? IExternal

        val hasSingle get() = single != null
        val hasEvent get() = event != null
        val hasState get() = state != null
        val hasGroup get() = group != null
        val hasStateGroup get() = stateGroup != null
        val hasEventGroup get() = eventGroup != null
        val hasExternal get() = external != null

        override suspend fun match(
            other: IConditionElement?,
            previousStateChanges: List<OnStateChanged>
        ): Boolean {
            return when {
                hasExternal -> external?.match(other, previousStateChanges) ?: false
                other is IComboElement -> {
                    when {
                        other.hasExternal -> other.external?.match(this, previousStateChanges) ?: false
                        else -> master.match(other.master, previousStateChanges)
                                && (slave == null && other.slave == null
                                || ignoreSlave || other.ignoreSlave
                                || slave?.match(other.slave, previousStateChanges) == true)
                    }
                }
                other is IInputElement -> other.match(this, previousStateChanges)
                other == null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            } logV {
                f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                m = "#CE $it => ${this@IComboElement} <||> $other"
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

        override suspend fun match(
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
                                || it.hasExternal && matchType == ALL
                    }
                    filtered.isEmpty() || when (matchType) {
                        ALL -> filtered.all { it.match(other, previousStateChanges) }
                        ANY -> filtered.any { it.match(other, previousStateChanges) }
                        NONE -> filtered.none { it.match(other, previousStateChanges) }
                    }
                }
                is IComboElement -> {
                    val filtered = elements.filter {
                        it.hasEvent && other.hasEvent
                                || it.hasState && other.hasState
                                || it.hasEventGroup && other.hasEventGroup
                                || it.hasStateGroup && other.hasStateGroup
                                || it.hasExternal && matchType == ALL
                    }
                    filtered.isEmpty() || when (matchType) {
                        ALL -> filtered.all { it.match(other, previousStateChanges) }
                        ANY -> filtered.any { it.match(other, previousStateChanges) }
                        NONE -> filtered.none { it.match(other, previousStateChanges) }
                    }
                }
                null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            } logV {
                f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                m = "#CG $it => ${this@IConditionElementGroup} <||> $other"
            }
        }
    }

    interface ICondition : IConditionElement {

        val items: List<IConditionElementGroup>
        val any: IConditionElementGroup get() = items.first { it.matchType == ANY }
        val all: IConditionElementGroup get() = items.first { it.matchType == ALL }
        val none: IConditionElementGroup get() = items.first { it.matchType == NONE }
        val action: (suspend ExecutorScope.() -> IComboElement?)?

        val start: IComboElement get() = items.first { it.matchType == ALL }.elements.first()

        enum class ConditionType {
            STATE,
            EVENT,
            EXTERNAL
        }

        val type: ConditionType
            get() = when (start.master) {
                is IEvent,
                is IEventGroup<IEvent> -> EVENT
                is IState,
                is IStateGroup<IState> -> ConditionType.STATE
                is IExternal -> EXTERNAL
                else -> throw IllegalArgumentException("Unexpected first element $start")
            }

        override suspend fun match(
            other: IConditionElement?,
            previousStateChanges: List<OnStateChanged>
        ): Boolean {
            return when (other) {
                is IInputElement -> {
                    any.match(other, previousStateChanges)
                            && all.match(other, previousStateChanges)
                            && none.match(other, previousStateChanges)
                }
                null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            } logV {
                f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                m = "#C $it => ${this@ICondition} <||> $other"
            }
        }
    }

    interface IInputElement : IConditionElement {

        val event: IComboElement
        val state: IComboElement

        override suspend fun match(other: IConditionElement?, previousStateChanges: List<OnStateChanged>): Boolean {
            return when (other) {
                is ICondition -> other.match(this, previousStateChanges)
                is IComboElement -> when {
                    other.hasEvent || other.hasEventGroup -> event.match(other, previousStateChanges)
                    other.hasState || other.hasStateGroup -> state.match(other, previousStateChanges)
                    else -> false
                }
                is IExternal -> other.match(this, previousStateChanges)
                null -> false
                else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
            } logV {
                f = GENERIC(disableLog = disableLogging || other.disableLogging || MachineEx.debugLevel <= INFO)
                m = "#IE $it => ${this@IInputElement} <||> $other"
            }
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

            /**
             * External condition.
             * Is checked at runtime. All External's need to match within a condition.
             */
            open class External(override val condition: suspend MatchScope.() -> Boolean) :
                Single(), IExternal {
                override fun toString(): String = "External"
            }
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
        override var usedAs: UsedAs = DEFINITION,
        override val ignoreSlave: Boolean = false,
        override var exclude: Boolean = false
    ) : IComboElement {
        override fun toString() = "CE($master|$slave|$ignoreSlave|$exclude|${when (master) {
            is IEvent -> "E"
            is IState -> "S"
            is IEventGroup<*> -> "Eg"
            is IStateGroup<*> -> "Sg"
            is IExternal -> "X"
            else -> "?[${master::class}]"
        }}${usedAs.name[0]})"
    }

    data class ConditionElementGroup(
        override val matchType: IConditionElementGroup.MatchType,
        override val elements: MutableList<IComboElement>
    ) : IConditionElementGroup {
        override fun toString(): String = "CG($matchType; $elements)"
    }

    data class Condition(
        override val items: List<IConditionElementGroup> = INITIAL_ITEMS,
        override val action: (suspend ExecutorScope.() -> IComboElement?)? = null
    ) : ICondition {

        constructor(master: IMaster) :
                this(INITIAL_ITEMS.apply { first { it.matchType == ALL }.elements.add(master.combo) })

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

val ICondition.isStateCondition get() = type == ICondition.ConditionType.STATE
val ICondition.isEventCondition get() = type == EVENT

val IComboElement.isDefinition get() = usedAs == DEFINITION
val IComboElement.isRuntime get() = usedAs == UsedAs.RUNTIME

val IComboElement.noLogging: Boolean get() = (master as? IEvent)?.noLogging == true

val IMaster.combo get() = ConditionElement.ComboElement(this)