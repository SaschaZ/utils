package dev.zieger.utils.core_testing

import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.channels.Channel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 *
 *
 * @param inputFactory Is called to create the input parameter [I] out of a [Map] of parameter names with their values.
 * Returned instance should be a class that uses [bind] to map the the input parameter to it's corresponding property in
 * the input class [I].
 * @param params Possible parameter names with a [List] of their possible values.
 * @param block Is called for every generated input parameter [I]. Must return it's result as a [List] of [R].
 *
 * @return [Map] of input values [I] mapped to their results of type [R].
 */
inline fun <I, R> parameterMixCollect(
    inputFactory: (Map<String, ParamInstance<*>>) -> I,
    vararg params: Pair<String, Param<*>>,
    block: I.() -> Channel<R>
): Map<I, Channel<R>> = params.toList().mix().runEach { inputFactory(this).run { block().let { this to it } } }.toMap()

inline fun <T> parameterMix(
    inputFactory: (Map<String, ParamInstance<*>>) -> T,
    vararg params: Pair<String, Param<*>>,
    block: T.() -> Unit
): Unit = parameterMixCollect(inputFactory, *params) { block(); Channel<Any>() }.asUnit()

fun List<Pair<String, Param<*>>>.mix(): List<Map<String, ParamInstance<*>>> {
    val expectedNumberCombinations: Int? = accumulate { accu, value -> (accu ?: 1) * value.second.list.size }
    println("number of combinations: $expectedNumberCombinations")

    val result: List<List<ParamInstance<*>>> =
        accumulate { accu, value -> (accu ?: ArrayList()).mixWithParam(value) } ?: emptyList()
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

/**
 * [ReadOnlyProperty] map the parameter to the corresponding property by their names.
 */
inline fun <reified T : Any?> bind(map: Map<String, ParamInstance<*>>) = object : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = map.getValue(property.name).value as T
}

inline fun <A, T> Collection<T>.accumulate(block: (A?, T) -> A): A? {
    var accu: A? = null
    forEach { accu = block(accu, it) }
    return accu
}