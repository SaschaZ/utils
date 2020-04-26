package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedScope

typealias Observer<T> = IOnChangedScope<Any?, T>.(T) -> Unit
typealias ObserverS<T> = suspend IOnChangedScope<Any?, T>.(T) -> Unit
typealias Observer2<P, T> = IOnChangedScope<P, T>.(T) -> Unit
typealias Observer2S<P, T> = suspend IOnChangedScope<P, T>.(T) -> Unit