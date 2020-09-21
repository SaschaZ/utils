package dev.zieger.utils.coroutines

import dev.zieger.utils.misc.nullWhen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Same as [withLock] but will execute the [block] even if the receiving [Mutex] is `null`.
 *
 * @param T Type to return inside [block] and from this method.
 *
 * @param bypass if `true` the [block] is executed directly without any lock. Defaulting to `false`.
 * @param block Any Lambda. The returned value is also return by this method.
 *
 * @return The returned value of [block].
 */
suspend inline fun <T> Mutex?.withLock(
    bypass: Boolean = false,
    block: () -> T
): T = this?.nullWhen { bypass }?.withLock { block() } ?: block()

/**
 * Executes [block] inside the lock of all receiving [Mutex]es.
 *
 * @param T Type to return inside [block] and from this method.
 *
 * @param bypass if `true` the [block] is executed directly without any lock. Defaulting to `false`.
 * @param block Any Lambda. The returned value is also return by this method.
 *
 * @return The returned value of [block].
 */
suspend inline fun <T> Iterable<Mutex>.withLock(
    bypass: Boolean = false,
    block: () -> T
): T {
    if (!bypass) forEach { it.lock() }
    val result = block()
    if (!bypass) forEach { it.unlock() }
    return result
}

suspend inline fun <K : Any, R : Any?> ConcurrentHashMap<K, Mutex>.withLock(
    key: K,
    bypass: Boolean = false,
    block: () -> R
): R = if (bypass) block() else getOrPut(key) { Mutex() }.withLock { block() }