package dev.zieger.utils.delegates


interface IScope2Factory<P : Any?, out T : Any?, out S : IOnChangedScope2<P, T>> {
    fun createScope(
        value: @UnsafeVariance T,
        thisRef: P?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean = false,
        setter: (T) -> Unit = {}
    ): S
}

class OnChangedScope2Factory<P : Any?, out T : Any?> : IScope2Factory<P, T, IOnChangedScope2<P, @UnsafeVariance T>> {
    override fun createScope(
        value: @UnsafeVariance T,
        thisRef: P?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean,
        setter: (T) -> Unit
    ): IOnChangedScope2<P, @UnsafeVariance T> =
        OnChangedScope2(value, thisRef, previousValue, previousValues, clearPreviousValues, isInitialNotification)
}

interface IScopeFactory<out T : Any?, out S : IOnChangedScope<T>> : IScope2Factory<Any?, T, S>

class OnChangedScopeFactory<out T : Any?> :
    IScopeFactory<T, IOnChangedScope<@UnsafeVariance T>> {
    override fun createScope(
        value: @UnsafeVariance T,
        thisRef: Any?,
        previousValue: @UnsafeVariance T?,
        previousValues: List<@UnsafeVariance T?>,
        clearPreviousValues: () -> Unit,
        isInitialNotification: Boolean,
        setter: (T) -> Unit
    ): IOnChangedScope<@UnsafeVariance T> =
        OnChangedScope(
            value,
            thisRef,
            previousValue,
            previousValues,
            clearPreviousValues,
            isInitialNotification
        )
}