package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.scopes.lvl0.ISetScope


interface IWithParameterScope {
    val params: List<Pair<String, String>>
}

class WithParameterScope(scope: IWithParameterScope) :
    IWithParameterScope by scope

infix fun ISetScope<IEvent>.withParameter(params: List<Pair<String, String>>) {
//    event.putAll(params)
}