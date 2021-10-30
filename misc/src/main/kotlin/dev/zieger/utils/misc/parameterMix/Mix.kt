@file:Suppress("unused")

package dev.zieger.utils.misc.parameterMix

import dev.zieger.utils.misc.DEFAULT_NUM_PARALLEL
import dev.zieger.utils.misc.mapParallel
import kotlinx.coroutines.flow.*

fun <T, R> mix(
    numParallel: Int = DEFAULT_NUM_PARALLEL,
    builder: ParameterBuilder.() -> Unit,
    instanceFactory: (Map<String, Number>) -> T,
    parameter: suspend T.(idx: Long) -> R
): Flow<Pair<Long, R>> {
    val params = ParameterBuilder().apply(builder).build().asFlow()
    return flow {
        var idx = 0L
        val combinations = params.combinations().map { map -> idx++ to map }
        emitAll(combinations.mapParallel(numParallel) { (i, values) ->
            val parameters = instanceFactory(values)
            i to parameters.parameter(i)
        }.filterNotNull())
    }
}

private suspend fun Flow<Parameter>.combinations(): Flow<Map<String, Number>> =
    fold(first().pairs) { pairs, param ->
        pairs.combine(param)
    }

private fun Parameter.combine(other: Parameter): Flow<Map<String, Number>> = pairs.combine(other)

private fun Flow<Map<String, Number>>.combine(other: Parameter): Flow<Map<String, Number>> = flow {
    onEmpty { this@flow.emitAll(other.pairs) }
    collect { params ->
        other.pairs.onEach { f ->
            f.forEach { pair ->
                emit(params + pair)
            }
        }.collect()
    }
}

private operator fun <K, V> Map<K, V>.plus(pair: Map.Entry<K, V>): Map<K, V> =
    (entries.map { (k, v) -> k to v } + (pair.key to pair.value)).toMap()

private val Parameter.pairs: Flow<Map<String, Number>>
    get() = values.asFlow().map { mapOf(property.name to it) }