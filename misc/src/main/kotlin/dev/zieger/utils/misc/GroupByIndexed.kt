package dev.zieger.utils.misc

fun <K, V> Iterable<V>.groupByIndexed(
    block: (idx: Int, value: V) -> K
): Map<K, List<V>> {
    var idx = 0
    return groupBy { block(idx++, it) }
}