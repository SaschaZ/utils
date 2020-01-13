package de.gapps.utils.misc

inline fun <T, K> List<T>.groupByPrev(keySelector: (cur: T, prev: T) -> K): Map<K, List<T>> {
    @Suppress("UNCHECKED_CAST")
    return groupByIndexed { index, value ->
        getOrNull(index - 1)?.let { keySelector(value, it) }
    }.filter { it.key != null } as Map<K, List<T>>
}

inline fun <T, K> Iterable<T>.groupByIndexed(keySelector: (index: Int, value: T) -> K): Map<K, List<T>> {
    var index = 0
    return groupBy { keySelector(index++, it) }
}