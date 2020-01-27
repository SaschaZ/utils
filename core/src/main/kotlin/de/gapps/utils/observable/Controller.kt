package de.gapps.utils.observable


typealias ControllerScope<T> = IControlledChangedScope<Any?, T>
typealias ControllerScope2<P, T> = IControlledChangedScope<P, T>

typealias Controller<T> = ControllerScope<T>.(T) -> Unit
typealias Controller2<P, T> = ControllerScope2<P, T>.(T) -> Unit