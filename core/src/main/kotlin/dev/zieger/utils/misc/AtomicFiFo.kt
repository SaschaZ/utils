@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.misc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

open class AtomicFiFo<T> private constructor(
    val capacity: Int,
    private val scope: CoroutineScope,
    private val mutex: Mutex,
    private val initial: List<T> = emptyList(),
    private val internal: MutableList<T>
) : List<T> by internal {

    constructor(capacity: Int, scope: CoroutineScope, mutex: Mutex = Mutex(), initial: List<T> = emptyList()) :
            this(capacity, scope, mutex, initial, LinkedList(initial))

    suspend fun put(value: T, update: Boolean = false): List<T> = mutex.withLock {
        when {
            update -> internal[internal.lastIndex] = value
            else -> {
                if (internal.size == capacity) internal.removeAt(0)
                internal += value
            }
        }
        internal
    }

    suspend fun take(): T? = mutex.withLock {
        when {
            internal.isEmpty() -> null
            else -> internal.removeAt(0)
        }
    }
}