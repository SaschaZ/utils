package de.gapps.utils.core_testing.assertion

interface IActualScope<out A : Any?> {
    var actual: @UnsafeVariance A
}

interface IActualMessageScope<out A : Any?, out E : Any?> : IActualScope<A> {
    val message: IValidationScope<@UnsafeVariance A, @UnsafeVariance E>.() -> String
}

class ActualMessageScope<out A : Any?, out E : Any?>(
    override var actual: @UnsafeVariance A,
    override val message: IValidationScope<@UnsafeVariance A, @UnsafeVariance E>.() -> String = { "" }
) : IActualMessageScope<A, E>