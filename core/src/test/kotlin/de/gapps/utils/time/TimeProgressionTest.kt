package de.gapps.utils.time

import de.gapps.utils.log.Log
import de.gapps.utils.time.duration.minutes
import de.gapps.utils.time.duration.weeks
import org.junit.Test

class TimeProgressionTest {

    @Test
    fun testInit() {
        val start = TimeEx() - 2.weeks
        val end = TimeEx()
        val step = 1.minutes
        (start..end step step * 750).forEach { Log.d("progression value=$it") }
    }
}