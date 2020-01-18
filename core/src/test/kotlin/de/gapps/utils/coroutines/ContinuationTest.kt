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
    }

    @Test
    fun testDirect() = runTest {
        continuation = Continuation()
        launchEx {
            continued = false
            continuation.suspendUntilTrigger()
            continued = true
        }

        delay(5.seconds)
        continued onFail "0" assert false
        continuation.trigger()
        delay(1.seconds)
        continued onFail "1" assert true
    }

    @Test
    fun testLambda() = runTest {
        lateinit var trigger: suspend () -> Unit
        launchEx {
            continued = false
            continueWhen {
                delay(6.seconds)
            }
            continued = true
        }

        delay(4.seconds)
        continued onFail "2" assert false
        delay(6.seconds)
        continued onFail "3" assert true
    }
}