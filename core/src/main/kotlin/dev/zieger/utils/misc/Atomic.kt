package dev.zieger.utils.misc

import java.util.concurrent.atomic.AtomicReference


fun <V> AtomicReference<V>.updateAndGetLegacy(updateFunction: (V) -> V): V {
    var prev: V?
    var next: V
    do {
        prev = get()
        next = updateFunction(prev)
    } while (!compareAndSet(prev, next))
    return next
}