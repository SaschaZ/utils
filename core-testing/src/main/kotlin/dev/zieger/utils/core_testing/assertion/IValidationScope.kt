package dev.zieger.utils.core_testing.assertion

interface IValidationScope<out A : Any?, out E : Any?> : ActualExpectedScope<A, E>, IActualMessageScope<A, E> {
    fun validate()
}