package dev.zieger.utils.core_testing.mix

import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.catch
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.channels.Channel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 *
 *
 * @param inputFactory Is called to create the input parameter [T] out of a [Map] of parameter names with their values.
 * Returned instance should be a class that uses [bind] to map the the input parameter to it's corresponding property in
 * the input class [T].
 * @param params Possible parameter names with a [List] of their possible values.
 * @param block Is called for every generated input parameter [T].
 */
inline fun <T> parameterMix(
    inputFactory: (Map<String, ParamInstance<*>>) -> T,
    vararg params: Pair<String, Param<*>>,
    block: T.() -> Unit
): Unit =
    parameterMixCollect(inputFactory, *params) { block(); Channel<Any>(Channel.UNLIMITED) }.asUnit()

/**
 *
 *
 * @param inputFactory Is called to create the input parameter [I] out of a [Map] of parameter names with their values.
 * Returned instance should be a class that uses [bind] to map the the input parameter to it's corresponding property in
 * the input class [I].
 * @param params Possible parameter names with a [List] of their possible values.
 * @param block Is called for every generated input parameter [I]. Must return it's result as a [List] of [R].
 *
 * @return [Map] of input values [I] mapped to a [List] of their results of type [R].
 */
inline fun <I, R> parameterMixCollect(
    inputFactory: (Map<String, ParamInstance<*>>) -> I,
    vararg params: Pair<String, Param<*>>,
    block: I.() -> Channel<R>
): Map<I, Channel<R>> =
    params.toList().buildParams().runEach {
        inputFactory(this).run input@{
            catch(this to Channel<R>().apply { close() }, onCatch = {
                throw AssertionError(
                    "failed with parameter:\n\t" +
                            "${toList().joinToString("\n\t") { (name, valueHolder) -> "$name: ${valueHolder.value}" }}\n" +
                            "${it.message}", it
                )
            }) { block().let { this to it } }
        }
    }.toMap()

fun List<Pair<String, Param<*>>>.buildParams(): List<Map<String, ParamInstance<*>>> {
    val expectedNumberCombinations: Long =
        fold(1L) { accu, (_, valueHolder) -> accu * valueHolder.amount }
    println("number of combinations: $expectedNumberCombinations")

    val result: List<List<ParamInstance<*>>> =
        fold(ArrayList<List<ParamInstance<*>>>() as List<List<ParamInstance<*>>>) { accu, (name, valueHolder) ->
            accu.mixWithParam(name, valueHolder)
        }
    if (result.size.toLong() != expectedNumberCombinations)
        throw IllegalStateException("Expected number of combinations does not match resulting number of combinations.")
    return result.map { it.map { i -> i.name to i }.toMap() }
}

private fun List<List<ParamInstance<*>>>.mixWithParam(
    name: String,
    values: Param<*>
): List<List<ParamInstance<*>>> {
    var idx = 0
    return if (isEmpty())
        (0 until values.amount).map {
            listOf(
                ParamInstance(name, values, idx++)
            )
        }
    else (this * values.amount).map {
        it + ParamInstance(name, values, idx++ / size)
    }
}

private operator fun <T> List<T>.times(factor: Int): List<T> {
    if (factor < 1) throw IllegalArgumentException("Factor is smaller than 1")
    var result = this
    (1 until factor).map { result = result + this }
    return result
}

/**
 * [ReadOnlyProperty] map the parameter to the corresponding property by their names.
 */
inline fun <reified T : Any?> bind(map: Map<String, ParamInstance<*>>) =
    object : ReadOnlyProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            map.getValue(property.name).value as T
    }
