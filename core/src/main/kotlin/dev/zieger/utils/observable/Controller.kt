package dev.zieger.utils.observable

typealias Controller<T> = IControlledChangedScope<Any?, T>.(T) -> Unit
typealias ControllerS<T> = suspend IControlledChangedScope<Any?, T>.(T) -> Unit
typealias Controller2<P, T> = IControlledChangedScope<P, T>.(T) -> Unit
typealias Controller2S<P, T> = suspend IControlledChangedScope<P, T>.(T) -> Unit