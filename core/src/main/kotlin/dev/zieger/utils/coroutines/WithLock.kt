package dev.zieger.utils.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap


suspend inline fun <T> Mutex?.withLock(block: () -> T): T = this?.withLock { block() } ?: block()

suspend inline fun <K : Any, R : Any?> ConcurrentHashMap<K, Mutex>.withLock(
    key: K,
    bypass: Boolean = false,
    block: () -> R
): R = if (bypass) block() else getOrPut(key) { Mutex() }.withLock { block() }

suspend inline fun Iterable<Mutex>.withLock(function: () -> Unit) {
    forEach { it.lock() }
    function()
    forEach { it.unlock() }
}