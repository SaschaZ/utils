package dev.zieger.utils.delegates


interface IScope2Factory<P : Any?, T : Any?> {
    fun createScope(
        value: T,
        thisRef: P?,
        previousValue: T?,
        previousValues: List<T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean = false,
        setter: (T) -> Unit = {}
    ): IOnChangedScope2<P, T>
}

class OnChangedScope2Factory<P : Any?, T : Any?> : IScope2Factory<P, T> {
    override fun createScope(
        value: T,
        thisRef: P?,
        previousValue: T?,
        previousValues: List<T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean,
        setter: (T) -> Unit
    ): IOnChangedScope2<P, T> =
        OnChangedScope2(value, thisRef, previousValue, previousValues, clearPreviousValues, isInitialNotification)
}

typealias IScopeFactory<T> = IScope2Factory<Any?, T>

typealias OnChangedScopeFactory<T> = OnChangedScope2Factory<Any?, T>