package dev.zieger.utils.log

import dev.zieger.utils.log.calls.logV
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.runBlocking


class TestLog : AnnotationSpec() {

    @Test
    fun testLog() = runBlocking {
        4 logV { "" }
    }
}