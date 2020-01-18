package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent


interface IOnScope<out E : IEvent>

class OnScope<out E : IEvent> : IOnScope<E>