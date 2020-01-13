package de.gapps.utils.machineex.scopes

import de.gapps.utils.machineex.IEvent


interface IOnScope<out E : IEvent>

class OnScope<out E : IEvent> : IOnScope<E>