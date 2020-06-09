@file:Suppress("unused")

package dev.zieger.utils.log

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec


internal class LogTest : AnnotationSpec() {

    @Test
    fun testSpamFilter() = runTest(15.seconds) {
        Log.w("before test")
    }
}