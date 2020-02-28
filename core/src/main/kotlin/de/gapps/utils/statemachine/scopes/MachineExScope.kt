package de.gapps.utils.statemachine.scopes

import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.IExecutionHolder
import de.gapps.utils.statemachine.scopes.definition.SInputDataHolder

open class MachineExScope<out E : IEvent, out S : IState>(
    override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    override val dataToExecutionMap: HashMap<SInputDataHolder<@UnsafeVariance E, @UnsafeVariance S>,
            IExecutionHolder<@UnsafeVariance S>> = HashMap()
) : IMachineExScope<E, S>