@file:Suppress("unused")

package dev.zieger.utils.log

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec

private val output = TestLogOutput()
private val cache = LogCache()

class GlobalLogScopeTest : AnnotationSpec() {

    @BeforeEach
    fun beforeEach() {
        output.reset()
        cache.reset()
        LogScope.configure(elements = LogElements(cache + LogScope), output = output)
    }

    @Test
    fun testGlobalLogScope() = runTest {
        Log.v("let's go")
        Log.i("to inform")

        delay(1.seconds)
        output.callCount assert 2
        cache.messages.count() assert 2
    }
}
