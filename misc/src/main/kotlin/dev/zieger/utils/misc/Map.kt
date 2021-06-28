package dev.zieger.utils.misc

inline fun <T: Any, O: Any> List<T?>.mapPrev(block: (cur: T, prev: T) -> O?): List<O> {
    var prev: T? = null
    return filterNotNull().mapNotNull { c ->
        prev?.let { p ->
            block(c, p).also { prev = c }
        } ?: run {
            prev = c
            null
        }
    }
}

inline fun <T: Any, O: Any> List<T>.runEach(block: T.() -> O): List<O> = map { it.block() }