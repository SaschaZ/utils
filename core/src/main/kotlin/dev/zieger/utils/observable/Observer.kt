package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedScope
import dev.zieger.utils.delegates.IOnChangedScope2

typealias Observer<T> = IOnChangedScope<T>.(T) -> Unit
typealias ObserverS<T> = suspend IOnChangedScope<T>.(T) -> Unit
typealias Observer2<P, T> = IOnChangedScope2<P, T>.(T) -> Unit
typealias Observer2S<P, T> = suspend IOnChangedScope2<P, T>.(T) -> Unit