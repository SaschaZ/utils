package de.gapps.utils.testing.assertion

interface IValidationScope<A : Any, E : Any> :
    ActualExpectedScope<A, E> {

    val message: ActualExpectedScope<A, E>.() -> String
    fun A.assertInternal()
}