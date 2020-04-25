package dev.zieger.utils.coroutines.channel.network

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

infix fun Port<*>.link(slave: Port<*>) = link(slave, DefaultCoroutineScope(), null)

fun Port<*>.link(
    slave: Port<*>,
    scope: CoroutineScope = DefaultCoroutineScope(),
    mutex: Mutex? = null
): Port<*> {
    val master = this
    if (master.type != slave.type)
        throw IllegalArgumentException("Can not link ports with different types ($type != ${slave.type})")

    Log.v("linking\n\t$master\n\twith\n\t$slave")
    scope.launchEx(mutex = mutex) {
        var idx = 0
        master.internalChannel.runEach {
            Log.v("$master\n\treceived $value\n\twill send to\n\t$slave")
            slave.internalChannel.send(NodeValue(outIdx, idx++, value, time))
        }
        Log.d("$master\n\tfinished processing\n\twill close\n\t$slave")
        slave.internalChannel.close()
    }
    return slave
}