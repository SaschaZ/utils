@file:Suppress("unused")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.runTest
import kotlin.test.assertEquals

class ParallelUniqueTest : ProcessorBaseTest(ParallelProcessingTypes.UNIQUE) {

    @Test
    fun testUniquePiped() = runTest {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount
        val idxGroups = consumeValues.groupBy { it.parallelIdx }
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.values.forEach { list ->
            assertEquals(list.size, testProducerAmount / numParallel)
        }
    }

    @Test
    fun testUniqueStandalone() = runTest {
        testParallelProcessor.run {
            testConsumer.run {
                testProducer.produce().process().consume().join()
            }
        }

        consumeValues.size assert testProducerAmount
        val idxGroups = consumeValues.groupBy { it.parallelIdx }
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.values.forEach { list ->
            assertEquals(list.size, testProducerAmount / numParallel)
        }
    }
}

