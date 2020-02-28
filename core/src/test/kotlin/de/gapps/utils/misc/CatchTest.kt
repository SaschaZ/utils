@file:Suppress("unused")

package de.gapps.utils.misc

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import org.junit.jupiter.api.Test

class CatchTest {

    @Test
    fun testCatch() {
        var caught = false
        var finally = false
        val result = catch(false, onCatch = { caught = true }, onFinally = { finally = true }) {
            throw RuntimeException("testException")
        }

        caught onFail "caught should be true" assert true
        finally onFail "finally should be true" assert true
        !result onFail "result should be false" assert true
    }
}