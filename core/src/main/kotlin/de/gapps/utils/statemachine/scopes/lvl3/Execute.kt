package de.gapps.utils.statemachine.scopes.lvl3

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.lvl4.EventChangeScope
import de.gapps.utils.statemachine.scopes.lvl4.IEventStateScope


infix fun <E : IEvent, S : IState> IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.execute(
    action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
) = machineExScope.addMapping(events, states, action)

infix fun <E : IEvent, S : IState> IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.executeOnly(
    action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> Unit
) = machineExScope.addMapping(events, states) { action(); states.first() }

infix fun <E : IEvent, S : IState> IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.executeS(
    action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
) = machineExScope.scope.launchEx { machineExScope.addMapping(events, states, action) }

infix fun <E : IEvent, S : IState> IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.executeOnlyS(
    action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> Unit
) = machineExScope.scope.launchEx {
    val listener: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S = {
        action()
        states.first()
    }
    machineExScope.addMapping(events, states, listener)
}

infix fun <E : IEvent, S : IState> IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.changeStateTo(toState: @UnsafeVariance S) =
    machineExScope.addMapping(events, states) { toState }