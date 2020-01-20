@file:Suppress("unused")

package de.gapps.utils.misc

import org.junit.Test
import kotlin.test.assertTrue

class CatchTest {

    @Test
    fun testCatch() {
        var caught = false
        var finally = false
        val result = catch(false, onCatch = { caught = true }, onFinally = { finally = true }) {
            throw RuntimeException("testException")
        }

        assertTrue(caught, "caught should be true")
        assertTrue(finally, "finally should be true")
        assertTrue(!result, "result should be false")
    }
}