package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.duration.seconds
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class ParallelSameTest : ProcessorBaseTest(
    ParallelProcessingType.SAME
) {

    @Test
    fun testSamePiped() = runTest(10.seconds) {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount * numParallel
        val idxGroups =
            consumeValues.groupBy { it.parallelIdx }.map { it.key to it.value.sorted() }.toMap()
        idxGroups.keys.size assert numParallel
        idxGroups.values.forEach { list ->
            list.size assert testProducerAmount
        }
    }

    @Test
    fun testSameStandalone() = runTest(10.seconds) {
        testParallelProcessor.run {
            testConsumer.run {
                testProducer.produce().process().consume().join()
            }
        }

        consumeValues.size assert testProducerAmount * numParallel
        val idxGroups =
            consumeValues.groupBy { it.parallelIdx }.map { it.key to it.value.sorted() }.toMap()
        idxGroups.keys.size assert numParallel
        idxGroups.values.forEach { list ->
            list.size assert testProducerAmount
        }
    }
}