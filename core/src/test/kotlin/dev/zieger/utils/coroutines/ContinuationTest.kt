@file:Suppress("unused", "LocalVariableName")

package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class ContinuationTest {

    private lateinit var continuation: Continuation
    private val continued = AtomicInteger(0)

    @BeforeEach
    fun before() {
        continuation = Continuation()
        continued.set(0)
    }

    @Test
    fun testDirect() = runTest {
        launchEx {
            continuation.suspend()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() assert 0 % "0"
        continuation.resume()
        delay(2.seconds)
        continued.get() assert 1 % "1"
    }

    @Test
    fun testMultiple() = runTest {
        launchEx {
            continuation.suspend()
            continued.incrementAndGet()
        }
        launchEx {
            continuation.suspend()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() assert 0 % "0"
        continuation.resume()
        delay(2.seconds)
        continued.get() assert 2 % "1"
    }
}