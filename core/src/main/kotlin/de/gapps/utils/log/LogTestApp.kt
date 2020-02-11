package de.gapps.utils.log

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.log.Filter.Companion.GENERIC
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.milliseconds
import de.gapps.utils.time.duration.minutes
import de.gapps.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking


internal object LogTestApp {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        repeat(10) { n ->
            launchEx {
                n logV { m = "das ist ein test $n"; f = GENERIC("foo", 1.seconds, resend = false) }
            }
            delay(300.milliseconds)
        }
    }.asUnit()
}

inline fun <T : AutoCloseable?, R> T.useRun(block: T.() -> R): R = use { run { block() } }
