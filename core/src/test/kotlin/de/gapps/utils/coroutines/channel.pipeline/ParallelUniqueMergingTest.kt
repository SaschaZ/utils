package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.runTest
import de.gapps.utils.time.duration.seconds
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ParallelUniqueMergingTest : ProcessorBaseTest(ParallelProcessingType.UNIQUE) {

    @Test
    fun testParallelUniqueMerging() = runTest(30.seconds) {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount

        var prevConsumed: IPipeValue<String>? = null
        consumeValues.forEach {
            assertTrue(
                prevConsumed == null
                        || it.outIdx >= prevConsumed?.outIdx ?: Integer.MAX_VALUE,
                "\ncurrent: $it\nprevious: $prevConsumed"
            )
            prevConsumed = it
        }

        val grouped = consumeValues.groupBy { it.parallelIdx }
        grouped.size assert numParallel

        val consumedSorted = consumeValues.sortedBy { it.inIdx }
        consumeValues.forEachIndexed { idx, value -> value assert consumedSorted[idx] }
    }
}

