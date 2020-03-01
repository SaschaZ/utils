package de.gapps.utils.statemachine.scopes.definition

import de.gapps.utils.statemachine.IState


interface IExecutionHolder<out RS : IState> {
    suspend fun execute(): IResultingStateHolder<RS>?
}

class ExecutionHolder<out RS : IState>(val toExecute: suspend () -> RS?) :
    IExecutionHolder<RS> {
    override suspend fun execute() = toExecute()?.let {
        ResultingStateHolder(
            it
        )
    }
}


interface IResultingStateHolder<out S : IState> {
    val resultingState: S
}

data class ResultingStateHolder<out S : IState>(override val resultingState: S) : IResultingStateHolder<S>