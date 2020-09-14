@file:Suppress("unused")

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

class TypeContinuationTest {

    private lateinit var continuation: TypeContinuation<Int>
    private val continued = AtomicInteger(0)

    @BeforeEach
    fun before() {
        continuation = TypeContinuation()
        continued.set(0)
    }

    @Test
    fun testDirect() = runTest {
        var result: Int? = null
        launchEx {
            result = continuation.suspendUntilTrigger()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() assert 0 % "0"
        continuation.trigger(100)
        delay(2.seconds)
        continued.get() assert 1 % "1"
        result assert 100
    }

    @Test
    fun testMultiple() = runTest {
        var result0: Int? = null
        launchEx {
            result0 = continuation.suspendUntilTrigger()
            continued.incrementAndGet()
        }
        var result1: Int? = null
        launchEx {
            result1 = continuation.suspendUntilTrigger()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() assert 0 % "0"
        continuation.trigger(100)
        delay(2.seconds)
        continued.get() assert 2 % "1"
        result0 assert 100
        result1 assert 100
    }
}