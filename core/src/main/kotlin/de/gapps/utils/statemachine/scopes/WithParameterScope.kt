package de.gapps.utils.statemachine.scopes


interface IWithParameterScope {
    val params: List<Pair<String, String>>
}

class WithParameterScope(scope: IWithParameterScope) :
    IWithParameterScope by scope

//infix fun <E: IEvent> IEventHolder<E>.withParameter(params: List<Pair<String, String>>) {
//    event.putAll(params)
//}