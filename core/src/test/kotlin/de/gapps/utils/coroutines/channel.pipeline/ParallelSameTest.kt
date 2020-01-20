package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.testing.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ParallelSameTest : ProcessorBaseTest(
    ParallelProcessingTypes.SAME
) {

    @Test
    fun testSamePiped() = runTest {
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
    fun testSameStandalone() = runTest {
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