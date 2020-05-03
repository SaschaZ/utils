package dev.zieger.utils.observable

typealias Controller<T> = IControlledChangedScope<T>.(T) -> Unit
typealias ControllerS<T> = suspend IControlledChangedScope<T>.(T) -> Unit
typealias Controller2<P, T> = IControlledChangedScope2<P, T>.(T) -> Unit
typealias Controller2S<P, T> = suspend IControlledChangedScope2<P, T>.(T) -> Unit