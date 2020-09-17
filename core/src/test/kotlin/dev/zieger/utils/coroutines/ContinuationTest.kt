@file:Suppress("unused", "LocalVariableName")

package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec
import java.util.concurrent.atomic.AtomicInteger

class ContinuationTest : AnnotationSpec() {

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
            continuation.suspendUntilTrigger()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() assert 0 % "0"
        continuation.trigger()
        delay(2.seconds)
        continued.get() assert 1 % "1"
    }

    @Test
    fun testMultiple() = runTest {
        launchEx {
            continuation.suspendUntilTrigger()
            continued.incrementAndGet()
        }
        launchEx {
            continuation.suspendUntilTrigger()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() assert 0 % "0"
        continuation.trigger()
        delay(2.seconds)
        continued.get() assert 2 % "1"
    }
}