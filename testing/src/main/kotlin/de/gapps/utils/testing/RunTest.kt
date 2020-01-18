package de.gapps.utils.testing

import de.gapps.utils.coroutines.withTimeout
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking

inline fun runTest(
    timeout: IDurationEx = 30.seconds,
    crossinline block: suspend () -> Unit
) {
    runBlocking { withTimeout(timeout) { block() } }
        .asUnit()
}