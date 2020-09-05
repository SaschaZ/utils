package dev.zieger.utils.time

import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.random.Random

internal class DurationFormatTest {

    @Test
    fun testFormat() {
        repeat(10) {
            println(
                Random.nextLong(50.years.millis).absoluteValue.toDuration()
                    .formatDuration(maxEntities = 9, sameLength = true)
            )
        }
    }
}