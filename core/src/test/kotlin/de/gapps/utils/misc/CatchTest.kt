@file:Suppress("unused")

package de.gapps.utils.misc

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.rem
import org.junit.jupiter.api.Test

class CatchTest {

    @Test
    fun testCatch() {
        var caught = false
        var finally = false
        val result = catch(false, onCatch = { caught = true }, onFinally = { finally = true }) {
            throw RuntimeException("testException")
        }

        caught assert true % "caught should be true"
        finally assert true % "finally should be true"
        !result assert true % "result should be false"
    }
}