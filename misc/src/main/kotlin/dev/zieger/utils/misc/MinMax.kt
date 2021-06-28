package dev.zieger.utils.misc



fun <T : Comparable<T>> min(vararg values: T?): T = minOrNull(*values)!!

fun <T : Comparable<T>> minOrNull(vararg values: T?): T? {
    var min: T? = null
    values.filterNotNull().forEach { v -> min = if (min?.let { it > v } != false) v else min }
    return min
}

fun <T : Comparable<T>> max(vararg values: T?): T = maxOrNull(*values)!!

fun <T : Comparable<T>> maxOrNull(vararg values: T?): T? {
    var max: T? = null
    values.filterNotNull().forEach { v -> max = if (max?.let { it < v } != false) v else max }
    return max
}