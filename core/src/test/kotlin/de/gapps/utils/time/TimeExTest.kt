package de.gapps.utils.time

import de.gapps.utils.time.base.minus
import de.gapps.utils.time.base.plus
import de.gapps.utils.time.base.div
import de.gapps.utils.time.base.times
import de.gapps.utils.time.duration.minutes
import de.gapps.utils.time.duration.toDuration
import de.gapps.utils.time.duration.years
import org.junit.Test

class TimeExTest {

    @Test
    fun testPrint() {
        val firstTime = TimeEx() - 5.years
        println("$firstTime")
        val secondTime = TimeEx() - 5.years * 10 / 3
        println("$secondTime")
        println("${10.minutes * 6}")
        val difference = firstTime - secondTime
        println("$difference")
    }
}