package de.gapps.utils.testing.assertion

interface IValidationScope<A : Any, E : Any> : ActualExpectedScope<A, E>, IActualMessageScope<A> {
    fun validate()
}