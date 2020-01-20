package de.gapps.utils.testing.assertion

interface IActualScope<A : Any> {
    var actual: A
}

interface IActualMessageScope<A : Any> : IActualScope<A> {
    val message: IValidationScope<A, A>.() -> String
}

class ActualMessageScope<A : Any>(
    override var actual: A,
    override val message: IValidationScope<A, A>.() -> String = { "" }
) : IActualMessageScope<A>