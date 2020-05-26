@file:Suppress("unused")

package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class ContinuationTest : AnnotationSpec() {

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
        continued assert false % "0"
        continuation.trigger()
        delay(1.seconds)
        continued assert true % "1"
    }
}