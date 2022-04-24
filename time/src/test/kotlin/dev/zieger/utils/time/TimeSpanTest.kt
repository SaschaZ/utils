package dev.zieger.utils.time

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class TimeSpanTest : AnnotationSpec() {

    @Test
    fun testFormat() {
        val duration = 0.minutes + 30.seconds
        println(duration.formatSpan(TimeUnit.MINUTE, TimeUnit.HOUR))
        duration.formatSpan(TimeUnit.MINUTE, TimeUnit.HOUR) shouldBe "0M"
    }
}