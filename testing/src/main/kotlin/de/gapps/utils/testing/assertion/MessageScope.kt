package de.gapps.utils.testing.assertion

interface IMessageScope<A : Any> {

    var actual: A
    val message: ActualExpectedScope<A, *>.() -> String
}

class MessageScope<A : Any>(
    override val message: ActualExpectedScope<A, *>.() -> String = { "" }
) : IMessageScope<A> {

    override lateinit var actual: A
}