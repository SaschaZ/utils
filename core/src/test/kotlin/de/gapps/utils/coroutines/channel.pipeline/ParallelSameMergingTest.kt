package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.runTest
import de.gapps.utils.time.duration.seconds
import org.junit.Test
import kotlin.test.assertTrue

class ParallelSameMergingTest : ProcessorBaseTest(ParallelProcessingType.SAME) {

    @Test
    fun testParallelSameMerging() = runTest(100.seconds) {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount * numParallel

        var prevConsumed: IPipeValue<String>? = null
        consumeValues.forEach {
//            Log.v("$it")
            assertTrue(
                it.outIdx >= prevConsumed?.outIdx ?: 0,
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