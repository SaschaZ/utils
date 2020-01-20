package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.runTest
import org.junit.Test
import kotlin.test.assertTrue

class ParallelSameMergingTest : ProcessorBaseTest(ParallelProcessingTypes.SAME) {

    @Test
    fun testParallelSameMerging() = runTest {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount * numParallel

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