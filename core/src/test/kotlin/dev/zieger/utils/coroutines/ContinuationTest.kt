@file:Suppress("unused")

package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.onFail
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContinuationTest {

    private lateinit var continuation: Continuation
    private var continued = false

    @BeforeEach
    fun before() = runBlocking {
        continuation = Continuation()
        continued = false
    }

    @Test
    fun testDirect() = runTest {
        launchEx {
            continuation.suspendUntilTrigger()
            continued = true
        }

        delay(2.seconds)
        continued onFail "0" assert false
        continuation.trigger()
        delay(1.seconds)
        continued onFail "1" assert true
    }
}