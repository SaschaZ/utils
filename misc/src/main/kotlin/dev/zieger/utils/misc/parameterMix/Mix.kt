@file:Suppress("unused")

package dev.zieger.utils.misc.parameterMix

import dev.zieger.utils.misc.DEFAULT_NUM_PARALLEL
import dev.zieger.utils.misc.mapParallel
import kotlinx.coroutines.flow.*

fun <T, R> mix(
    numParallel: Int = DEFAULT_NUM_PARALLEL,
    builder: ParameterBuilder.() -> Unit,
    instanceFactory: (Map<String, Any?>) -> T,
    parameter: suspend T.(idx: Long) -> R
): Flow<Triple<Long, Long, R>> {
    val params = ParameterBuilder().apply(builder).build().asFlow()
    return flow {
        var idx = 0L
        val combinations = params.combinations().map { map -> idx++ to map }.toList().asFlow()
        emitAll(combinations.mapParallel(numParallel) { (i, values) ->
            val parameters = instanceFactory(values)
            Triple(i, idx, parameters.parameter(i))
        }.filterNotNull())
    }
}

private suspend fun <T> Flow<Parameter<out T>>.combinations(): Flow<Map<String, T>> {
    var firstDone = false
    return fold(first().pairs) { pairs, param ->
        if (!firstDone) {
            firstDone = true
            pairs
        } else pairs.combine(param)
    }
}

private fun <T> Parameter<out T>.combine(other: Parameter<out T>): Flow<Map<String, T>> = pairs.combine(other)

private fun <T> Flow<Map<String, T>>.combine(other: Parameter<out T>): Flow<Map<String, T>> = flow {
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

private val <T> Parameter<out T>.pairs: Flow<Map<String, T>>
    get() = values.asFlow().map { mapOf(property.name to it) }