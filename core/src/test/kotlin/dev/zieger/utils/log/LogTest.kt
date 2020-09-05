@file:Suppress("unused")

package dev.zieger.utils.log

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.seconds
import org.junit.jupiter.api.Test


internal class LogTest {

    @Test
    fun testSpamFilter() = runTest(15.seconds) {
        Log.w("before test")
    }
}