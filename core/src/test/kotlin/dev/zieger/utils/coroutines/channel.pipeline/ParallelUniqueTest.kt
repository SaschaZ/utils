@file:Suppress("unused")

package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import io.kotlintest.specs.AbstractAnnotationSpec.Test

class ParallelUniqueTest : ProcessorBaseTest(ParallelProcessingType.UNIQUE) {

    @Test
    fun testUniquePiped() = runTest {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount
        val idxGroups = consumeValues.groupBy { it.parallelIdx }
        idxGroups.keys.size assert numParallel
        idxGroups.values.forEach { list ->
            list.size assert (testProducerAmount / numParallel)
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
        idxGroups.keys.size assert numParallel
        idxGroups.values.forEach { list ->
            list.size assert (testProducerAmount / numParallel)
        }
    }
}

