package dev.zieger.utils.log

import dev.zieger.utils.log.calls.logV
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.runBlocking


class TestLog : AnnotationSpec() {


    val Any.logV get() = Log.v(this)

    @Test
    fun testLog() = runBlocking {
        4 logV { "$it" }
        4.logV

    }

    @Test
    fun testError() = runBlocking {
        Log.e(IllegalStateException("Test Exception"))
    }
}