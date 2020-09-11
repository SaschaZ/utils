@file:Suppress("unused")

package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec

class ContinuationTest : AnnotationSpec() {

    private lateinit var continuation: Continuation
    private var continued = 0

    @BeforeEach
    fun before() {
        continuation = Continuation()
        continued = 0
    }

    @Test
    fun testDirect() = runTest {
        launchEx {
            continuation.suspendUntilTrigger()
            continued++
        }

        delay(2.seconds)
        continued assert 0 % "0"
        continuation.trigger()
        delay(1.seconds)
        continued assert 1 % "1"
    }

    @Test
    fun testMultiple() = runTest {
        launchEx {
            continuation.suspendUntilTrigger()
            continued++
        }
        launchEx {
            continuation.suspendUntilTrigger()
            continued++
        }

        delay(2.seconds)
        continued assert 0 % "0"
        continuation.trigger()
        delay(1.seconds)
        continued assert 2 % "1"
    }
}