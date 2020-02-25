package de.gapps.utils.log

import de.gapps.utils.testing.runTest
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.milliseconds
import de.gapps.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec

internal class LogFilterProxyTest : AnnotationSpec() {

    @Test
    fun testMessageWrapper() = runTest(100.seconds) {
        println("before test")

        repeat(100) {
            Log.v("das ist ein test $it", filter = Filter.Companion.GENERIC("someId", 1.seconds, false, false))
            delay(100.milliseconds)
        }
    }
}