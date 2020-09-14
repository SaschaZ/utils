package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import java.util.*

open class TypeContinuation<T>(override val scope: CoroutineScope = DefaultCoroutineScope()) : IContinuationBase<T> {

    private val channel = LinkedList<Channel<T>>()
    private val mutex = Mutex()

    override suspend fun suspendUntilTrigger(
        wanted: T?,
        timeout: IDurationEx?
    ): T = withTimeout(timeout) {
        var result: T
        val c = Channel<T>()
        mutex.withLock {
            channel += c
        }
        do {
            result = c.receive()
        } while (wanted?.let { result != it } == true)
        result
    }

    override suspend fun trigger(value: T) = mutex.withLock {
        channel.runEach {
            send(value)
            close()
        }
        channel.clear()
    }.asUnit()
}