package dev.zieger.utils.time

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx
import dev.zieger.utils.time.progression.step
import org.junit.jupiter.api.Test

class TimeProgressionTest {

    @Test
    fun testInit() {
        val end: ITimeEx = TimeEx()
        val start: ITimeEx = end - 2.weeks
        val step: IDurationEx = 1.minutes
        (start..end step (step * 750)).toList().also {
            it.size assert (2.weeks / (step * 750)).toInt()
        }
    }
}