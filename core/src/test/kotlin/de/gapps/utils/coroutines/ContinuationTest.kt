@file:Suppress("unused")

package de.gapps.utils.coroutines

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.assertion.onFail
import de.gapps.utils.testing.runTest
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class ContinuationTest : AnnotationSpec() {

    private lateinit var continuation: Continuation
    private var continued = false

    @Before
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