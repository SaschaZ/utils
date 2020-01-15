@file:Suppress("unused")

package de.gapps.utils.testing

import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout


fun runTest(
    timeout: IDurationEx = 5.seconds,
    block: suspend () -> Unit
) {
    runBlocking { withTimeout(timeout.millis) { block() } }.asUnit()
}