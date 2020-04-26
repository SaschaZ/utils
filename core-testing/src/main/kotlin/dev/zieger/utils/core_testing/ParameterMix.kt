package dev.zieger.utils.core_testing

import dev.zieger.utils.misc.runEach
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


inline fun <reified T : Any> parameterMix(
    contextBuilder: (Map<String, ParamInstance<*>>) -> T,
    vararg params: Pair<String, Param<*>>,
    block: T.() -> Unit
) = params.toList().mix().runEach { contextBuilder(this).block() }

fun List<Pair<String, Param<*>>>.mix(): List<Map<String, ParamInstance<*>>> {
    var expectedNumberCombinations = 1
    forEach { expectedNumberCombinations *= it.second.list.size }
    println("number of combinations: $expectedNumberCombinations")

    var result: List<List<ParamInstance<*>>> = emptyList()
    forEach { result = result.mixWithParam(it) }
    if (result.size != expectedNumberCombinations)
        throw IllegalStateException("Expected number of combinations does not match resulting number of combinations.")
    return result.map { it.map { i -> i.name to i }.toMap() }
}

private fun List<List<ParamInstance<*>>>.mixWithParam(param: Pair<String, Param<*>>): List<List<ParamInstance<*>>> {
    var idx = 0
    return if (isEmpty())
        param.second.list.map {
            listOf(
                ParamInstance(
                    param.first,
                    param.second,
                    idx++
                )
            )
        }
    else (this * param.second.list.size).map {
        it + ParamInstance(
            param.first,
            param.second,
            idx++ / size
        )
    }
}

operator fun <T> List<T>.times(factor: Int): List<T> {
    if (factor < 1) throw IllegalArgumentException("Factor is smaller than 1")
    var result = this
    (1 until factor).map { result = result + this }
    return result
}

data class ParamInstance<T : Any>(
    val name: String,
    val type: KClass<T>,
    val value: T?
) {
    constructor(name: String, param: Param<T>, idx: Int) :
            this(name, param.type, param.list[idx % param.list.size])

    override fun toString(): String = "$value"
}

data class Param<T : Any>(
    val type: KClass<T>,
    val list: List<T?>
)

inline fun <reified T : Any> param(name: String, vararg value: T?) =
    name to Param(T::class, value.toList())

inline fun <reified T : Any> param(name: String, list: List<T?>) =
    name to Param(T::class, list)

inline fun <reified T : Any> param(name: String, iterable: Iterable<T?>) =
    name to Param(T::class, iterable.toList())

inline fun <reified T : Any?> bind(map: Map<String, ParamInstance<*>>) = object : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = map.getValue(property.name).value as T
}