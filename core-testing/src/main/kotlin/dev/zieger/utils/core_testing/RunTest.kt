package dev.zieger.utils.core_testing

import dev.zieger.utils.UtilsSettings
import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.seconds
import kotlinx.coroutines.runBlocking

/**
 * Helper method to execute tests with a timeout.
 *
 * Usage:
 * ```
 * @Test
 * fun testSomething() = runTest {
 *   1 isEqual 1
 * }
 * ```
 *
 * @param timeout Timeout after the test will fail. Defaulting to 10 seconds.
 * @param block Suspend lambda for the test.
 */
fun runTest(
    timeout: IDurationEx = 10.seconds,
    block: suspend () -> Unit
) = runBlocking {
    UtilsSettings.LOG_EXCEPTIONS = false
    UtilsSettings.PRINT_EXCEPTIONS = false
    withTimeout(timeout) { block() }
}.asUnit()