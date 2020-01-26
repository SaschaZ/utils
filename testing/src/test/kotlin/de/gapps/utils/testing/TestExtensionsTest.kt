package de.gapps.utils.testing

import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.assertion.onFail
import org.junit.Test

class TestExtensionsTest {

    @Test
    fun testRunTest() = runTest {
        9 onFail "boooo" assert 9
    }
}