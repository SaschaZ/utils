@file:Suppress("unused")

package de.gapps.utils.machineex.misc

import de.gapps.utils.misc.catch
import io.kotlintest.specs.AnnotationSpec
import kotlin.test.assertTrue

class CatchTest : AnnotationSpec() {

    @Test
    fun testCatch() {
        var caught = false
        var finally = false
        val result = catch(false, onCatch = { caught = true }, onFinally = { finally = true }) {
            throw RuntimeException("testException")
            true
        }

        assertTrue(caught, "caught should be true")
        assertTrue(finally, "finally should be true")
        assertTrue(!result, "result should be false")
    }
}