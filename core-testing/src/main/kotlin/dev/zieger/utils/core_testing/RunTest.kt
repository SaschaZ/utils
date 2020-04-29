package dev.zieger.utils.core_testing

import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking

fun runTest(
    timeout: IDurationEx = 5.seconds,
    block: suspend () -> Unit
) = runBlocking { withTimeout(timeout) { block() } }.asUnit()