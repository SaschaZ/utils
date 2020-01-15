package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.equals
import de.gapps.utils.testing.runTest
import kotlin.test.assertEquals

class PipelineTest : ProcessorBaseTest() {

    @Test
    fun testPipeline() = runTest {
        val parallelTestProcessor = ParallelProcessor(params) { testProcessors[it] }

        testProducer + parallelTestProcessor + testConsumer

        assertEquals(testProducerAmount, consumeValues.size)
        consumeValues.forEachIndexed { index, i -> index equals i.inIdx }
    }
}