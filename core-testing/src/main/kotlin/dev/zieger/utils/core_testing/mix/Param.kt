package dev.zieger.utils.core_testing.mix

import kotlin.reflect.KClass

class Param<T : Any>(
    val type: KClass<T>,
    val amount: Int = 1,
    val block: (Int) -> T?
) {
    constructor(
        type: KClass<T>,
        list: List<T?>
    ) : this(type, list.size, { list[it % list.size] })

    operator fun get(idx: Int): T? = block(idx)

    val list: List<T?> get() = (0..amount).map(block)
}

data class ParamInstance<T : Any>(
    val name: String,
    val type: KClass<T>,
    val value: T?
) {
    constructor(name: String, param: Param<T>, idx: Int) :
            this(name, param.type, param[idx % param.amount])

    override fun toString(): String = "$value"
}

inline fun <reified T : Any> param(name: String, vararg value: T?) =
    name to Param(T::class, value.toList())

inline fun <reified T : Any> param(name: String, list: List<T?>) =
    name to Param(T::class, list)

inline fun <reified T : Any> param(name: String, iterable: Iterable<T?>) =
    name to Param(T::class, iterable.toList())

inline fun <reified T : Any> param(name: String, amount: Int = 1, noinline block: (Int) -> T?) =
    name to Param(T::class, amount, block)
