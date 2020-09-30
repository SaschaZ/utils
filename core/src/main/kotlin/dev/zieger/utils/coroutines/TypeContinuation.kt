package dev.zieger.utils.coroutines

import dev.zieger.utils.misc.runEach
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.channels.Channel
import java.util.*

open class TypeContinuation<T> : IContinuationBase<T> {

    private var channel = LinkedList<Channel<T>>()

    override suspend fun suspendUntilTrigger(
        wanted: T?,
        timeout: IDurationEx?
    ): T = withTimeout(timeout) {
        var result: T
        val c = Channel<T>()
        channel.add(c)
        do {
            result = c.receive()
        } while (wanted?.let { result != it } == true)
        c.close()
        result
    }

    override fun trigger(value: T) {
        val tmp = channel
        channel = LinkedList()

        tmp.runEach {
            offer(value)
        }
    }
}