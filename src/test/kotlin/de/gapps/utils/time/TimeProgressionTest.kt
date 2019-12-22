package de.gapps.utils.machineex.time

import de.gapps.utils.misc.Log
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.minutes
import de.gapps.utils.time.duration.weeks
import de.gapps.utils.time.step
import io.kotlintest.specs.AnnotationSpec

class TimeProgressionTest : AnnotationSpec() {

    @Test
    fun testInit() {
        val start = TimeEx() - 2.weeks
        val end = TimeEx()
        val step = 1.minutes
        (start..end step step * 750).forEach { Log.d("progression value=$it") }
    }
}