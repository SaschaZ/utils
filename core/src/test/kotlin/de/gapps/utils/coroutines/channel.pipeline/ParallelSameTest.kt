package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.testing.runTest
import de.gapps.utils.time.duration.seconds
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParallelSameTest : ProcessorBaseTest(
    ParallelProcessingType.SAME
) {

    @Test
    fun testSamePiped() = runTest(10.seconds) {
        testProducer + testParallelProcessor + testConsumer

        assertEquals(consumeValues.size, testProducerAmount * numParallel)
        val idxGroups =
            consumeValues.groupBy { it.parallelIdx }.map { it.key to it.value.sorted() }.toMap()
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.values.forEach { list ->
            assertEquals(list.size, testProducerAmount)
        }
    }

    @Test
    fun testSameStandalone() = runTest(10.seconds) {
        testParallelProcessor.run {
            testConsumer.run {
                testProducer.produce().process().consume().join()
            }
        }

        assertEquals(consumeValues.size, testProducerAmount * numParallel)
        val idxGroups =
            consumeValues.groupBy { it.parallelIdx }.map { it.key to it.value.sorted() }.toMap()
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.values.forEach { list ->
            assertEquals(list.size, testProducerAmount)
        }
    }
}