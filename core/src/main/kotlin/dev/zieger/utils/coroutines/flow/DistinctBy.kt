package dev.zieger.utils.coroutines.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import java.util.*

fun <T> Flow<T>.distinctBy(block: (T) -> Any): Flow<T> {
    val keys = LinkedList<Any>()
    return filterNot { item ->
        block(item).let { b -> keys.contains(b).also { r -> if (r) keys -= b else keys += b } }
    }
}