package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent


interface IWithParameterScope<out K : Any, out V : Any> {
    val params: List<Pair<K, V>>
}

class WithParameterScope<out K : Any, out V : Any>(scope: IWithParameterScope<K, V>) :
    IWithParameterScope<K, V> by scope

infix fun <K : Any, V : Any> ISetScope<K, V, IEvent<K, V>>.withParameter(params: List<Pair<K, V>>) {
    event.putAll(params)
}