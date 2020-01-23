package de.gapps.utils.coroutines.channel.pipeline.parallel

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

open class SortedArrayList<T : Comparable<T>> : LinkedList<T>() {

    private val mutex = Mutex()

    suspend fun addSync(element: T) = mutex.withLock {
        forEachIndexed { index, t ->
            if (t > element) {
                add(index, element)
                return true
            }
        }
        add(element)
    }

    suspend fun removeFirstSync(): T = mutex.withLock {
        removeFirst()
    }
}