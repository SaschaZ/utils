package de.gapps.utils.testing

import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.assertion.onFail
import io.kotlintest.specs.AnnotationSpec

class TestExtensionsTest : AnnotationSpec() {

    @Test
    fun testRunTest() = runTest {
        6 onFail "boooo" assert 9
    }
}