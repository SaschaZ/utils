@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.time.progression.step
import io.kotest.assertions.eq.eq
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.*

internal class TimeStampTest : AnnotationSpec() {

    @Test
    fun testEmptyConstructor() {
        TimeStamp.DEFAULT_TIME_ZONE = TimeZone.getDefault()
        println(TimeStamp())
    }

    @Test
    fun testStringInit() {
        val timeStr = "1.1.2021-00:00:00"
        timeStr shouldBe timeStr.parse()
    }

    @Test
    fun testAddMonth() {
        val time = TimeStamp("5.10.2020-00:00:00", UTC)
        time.formatTime() shouldBe "05.10.2020-00:00:00"

        time.hourOfDay shouldBe 0
        time.minusMonths(0).formatTime() shouldBe "05.10.2020-00:00:00"
        time.plusMonths(6).formatTime() shouldBe "05.04.2021-00:00:00"
        time.plusMonths(12).formatTime() shouldBe "05.10.2021-00:00:00"
        time.plusMonths(-6).formatTime() shouldBe "05.04.2020-00:00:00"
        time.plusMonths(-12).formatTime() shouldBe "05.10.2019-00:00:00"

        time.minusMonths(6).formatTime() shouldBe "05.04.2020-00:00:00"
        time.minusMonths(12).formatTime() shouldBe "05.10.2019-00:00:00"
        time.minusMonths(-6).formatTime() shouldBe "05.04.2021-00:00:00"
        time.minusMonths(-12).formatTime() shouldBe "05.10.2021-00:00:00"

        time.minusMonths(18).formatTime() shouldBe "05.04.2019-00:00:00"
        time.minusMonths(30).formatTime() shouldBe "05.04.2018-00:00:00"
    }

    @Test
    fun testTimeProgression() {
        val start = "1.4.2021".parse()
        val end = start + 3.days
        (start..end step 1.days).toList().size shouldBe 4
    }

    @Test
    fun testHashCode() {
        "1.4.2021".parse().hashCode() shouldBe "1.4.2021".parse().hashCode()
    }
}