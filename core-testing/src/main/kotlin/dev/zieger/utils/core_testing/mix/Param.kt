package dev.zieger.utils.core_testing.mix

import kotlin.reflect.KClass

data class Param<T : Any>(
    val type: KClass<T>,
    val list: List<T?>
)

data class ParamInstance<T : Any>(
    val name: String,
    val type: KClass<T>,
    val value: T?
) {
    constructor(name: String, param: Param<T>, idx: Int) :
            this(name, param.type, param.list[idx % param.list.size])

    override fun toString(): String = "$value"
}

inline fun <reified T : Any> param(name: String, vararg value: T?) =
    name to Param(T::class, value.toList())

inline fun <reified T : Any> param(name: String, list: List<T?>) =
    name to Param(T::class, list)

inline fun <reified T : Any> param(name: String, iterable: Iterable<T?>) =
    name to Param(T::class, iterable.toList())