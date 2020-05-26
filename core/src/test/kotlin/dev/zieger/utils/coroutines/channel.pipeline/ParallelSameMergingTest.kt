package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AbstractAnnotationSpec.Test

class ParallelSameMergingTest : ProcessorBaseTest(ParallelProcessingType.SAME) {

    @Test
    fun testParallelSameMerging() = runTest(100.seconds) {
        testProducer + testParallelProcessor + testConsumer

        consumeValues.size assert testProducerAmount * numParallel

        var prevConsumed: IPipeValue<String>? = null
        consumeValues.forEach {
//            Log.v("$it")
            (it.outIdx >= prevConsumed?.outIdx ?: 0) assert true
            prevConsumed = it
        }

        val grouped = consumeValues.groupBy { it.parallelIdx }
        grouped.size assert numParallel

        val consumedSorted = consumeValues.sortedBy { it.inIdx }
        consumeValues.forEachIndexed { idx, value -> value assert consumedSorted[idx] }
    }
}