package de.gapps.utils.core_testing

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import org.junit.jupiter.api.Test

class TestExtensionsTest {

    @Test
    fun testRunTest() = runTest {
        9 onFail "boooo" assert 9
    }
}