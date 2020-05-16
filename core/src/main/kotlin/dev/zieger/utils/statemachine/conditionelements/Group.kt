package dev.zieger.utils.statemachine.conditionelements

import kotlin.reflect.KClass

interface IGroup<out T : ISingle> : IMaster {
    val type: KClass<@UnsafeVariance T>
}

abstract class Group<out T : ISingle>(override val type: KClass<@UnsafeVariance T>) : Master(), IGroup<T>