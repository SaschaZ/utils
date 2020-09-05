package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.seconds
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class ParallelUniqueMergingTest : ProcessorBaseTest(ParallelProcessingType.UNIQUE) {

    @Test
    fun testParallelUniqueMerging() = runTest(30.seconds) {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount

        var prevConsumed: IPipeValue<String>? = null
        consumeValues.forEach {
            (prevConsumed == null
                    || it.outIdx >= prevConsumed?.outIdx ?: Integer.MAX_VALUE) assert true
            prevConsumed = it
        }

        val grouped = consumeValues.groupBy { it.parallelIdx }
        grouped.size assert numParallel

        val consumedSorted = consumeValues.sortedBy { it.inIdx }
        consumeValues.forEachIndexed { idx, value -> value assert consumedSorted[idx] }
    }
}

