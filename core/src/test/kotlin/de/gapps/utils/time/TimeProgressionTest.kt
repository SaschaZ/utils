package de.gapps.utils.time

import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.time.base.div
import de.gapps.utils.time.base.minus
import de.gapps.utils.time.base.times
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.minutes
import de.gapps.utils.time.duration.weeks
import de.gapps.utils.time.progression.step
import org.junit.jupiter.api.Test

class TimeProgressionTest {

    @Test
    fun testInit() {
        val end: ITimeEx = TimeEx()
        val start: ITimeEx = end - 2.weeks
        val step: IDurationEx = 1.minutes
        (start..end step (step * 750)).toList().also {
            it.size assert (2.weeks / (step * 750)).millis.toInt()
        }
    }
}