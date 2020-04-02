package dev.zieger.utils

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking


internal object LogTestApp {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        repeat(10) { n ->
            launchEx {
                n logV { m = "das ist ein test $n"; f = GENERIC("foo", 1.seconds) }
            }
            delay(300.milliseconds)
        }
    }.asUnit()
}

inline fun <T : AutoCloseable?, R> T.useRun(block: T.() -> R): R = use { run { block() } }
