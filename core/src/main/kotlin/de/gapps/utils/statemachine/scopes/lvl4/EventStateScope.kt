package de.gapps.utils.statemachine.scopes.lvl4

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.lvl1.IEventScope
import de.gapps.utils.statemachine.scopes.definition.lvl1.IEventTypeScope
import kotlin.reflect.KClass


interface IEventStateScope<out E : IEvent, out S : IState> :
    IEventScope<E, S> {
    val states: List<@UnsafeVariance S>
    val state: S?

    infix fun execute(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) = machineScope.addMappingValue(events, states, action)

    infix fun executeOnly(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> Unit
    ) = machineScope.addMappingValue(events, states) { action(); states.first() }

    infix fun executeS(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) = machineScope.scope.launchEx { machineScope.addMappingValue(events, states, action) }

    infix fun executeOnlyS(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> Unit
    ) = machineScope.scope.launchEx {
        val listener: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S = {
            action()
            states.first()
        }
        machineScope.addMappingValue(events, states, listener)
    }

    infix fun changeStateTo(toState: @UnsafeVariance S) =
        machineScope.addMappingValue(events, states) { toState }
}

class EventStateScope<out E : IEvent, out S : IState>(
    eventScope: IEventScope<E, S>,
    override val states: List<@UnsafeVariance S>
) : IEventStateScope<E, S>, IEventScope<E, S> by eventScope {

    override val state: S?
        get() = states.firstOrNull()
}

interface IEventTypeStateScope<out E : IEvent, out S : IState> :
    IEventTypeScope<E, S> {
    val stateTypes: List<KClass<@UnsafeVariance S>>
    val stateType: KClass<@UnsafeVariance S>

    infix fun execute(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) = machineScope.addMappingType(eventTypes, stateTypes, action)

    infix fun executeOnly(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> Unit
    ) = machineScope.addMappingType(eventTypes, stateTypes) { action(); state }

    infix fun executeS(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) = machineScope.scope.launchEx { machineScope.addMappingType(eventTypes, stateTypes, action) }

    infix fun executeOnlyS(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> Unit
    ) = machineScope.scope.launchEx {
        val listener: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S = {
            action()
            state
        }
        machineScope.addMappingType(eventTypes, stateTypes, listener)
    }

    infix fun changeStateTo(toState: @UnsafeVariance S) =
        machineScope.addMappingType(eventTypes, stateTypes) { toState }
}

class EventTypeStateScope<out E : IEvent, out S : IState>(
    eventTypeScope: IEventTypeScope<E, S>,
    override val stateTypes: List<KClass<@UnsafeVariance S>>
) : IEventTypeStateScope<E, S>, IEventTypeScope<E, S> by eventTypeScope {

    override val stateType: KClass<@UnsafeVariance S>
        get() = stateTypes.first()
}