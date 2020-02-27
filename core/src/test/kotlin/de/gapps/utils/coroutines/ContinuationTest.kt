@file:Suppress("unused")

package de.gapps.utils.coroutines

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.seconds
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