package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChangedScope


typealias ObserverScope<T> = IOnChangedScope<Any?, T>
typealias ObserverScope2<P, T> = IOnChangedScope<P, T>

typealias Observer<T> = ObserverScope<T>.(T) -> Unit
typealias Observer2<P, T> = ObserverScope2<P, T>.(T) -> Unit