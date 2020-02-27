package de.gapps.utils.core_testing.assertion

interface IValidationScope<A : Any?, E : Any?> : ActualExpectedScope<A, E>, IActualMessageScope<A> {
    fun validate()
}