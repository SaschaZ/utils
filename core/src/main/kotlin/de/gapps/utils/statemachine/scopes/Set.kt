@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx

val <D : Any> IMachineEx<D>.set get() = SetScope(this)

class SetScope<out D : Any>(machine: IMachineEx<D>) : IMachineEx<D> by machine {

    infix fun event(event: IEvent<@UnsafeVariance D>) {
        this.event = event
    }

    suspend infix fun eventSync(event: IEvent<@UnsafeVariance D>) {
        event(event)
        suspendUtilProcessingFinished()
    }
}

operator fun <D : Any> IEvent<@UnsafeVariance D>.plus(data: @UnsafeVariance D?): IEvent<D> {
    this.data = data
    return this
}

operator fun <D : Any> Set<IEvent<@UnsafeVariance D>>.plus(data: D?): Set<IEvent<@UnsafeVariance D>> {
    forEach { it.data = data }
    return this
}