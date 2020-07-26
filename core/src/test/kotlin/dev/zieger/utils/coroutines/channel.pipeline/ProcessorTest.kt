package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.misc.runEachIndexed
import dev.zieger.utils.time.minutes
import io.kotlintest.specs.AbstractAnnotationSpec.Test
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay

class ProcessorTest : IParamsHolder {

    override val params: IProcessingParams = ProcessingParams()

    private val testProducer = producer<Int> {
        repeat(500) { send(it); delay(10) }
        close()
    }

    @Test
    fun testProcessor() = runTest(1.minutes) {
        var finished = false
        val processorResult = object : Processor<Int, Int>(params, block = {
            send(it)
            delay(10)
        }, identity = NoId) {
            override suspend fun IProducerScope<Int>.onProcessingFinished() {
                finished = true
            }
        }.run {
            testProducer.produce().process().toList()
        }
        finished assert true
        processorResult.size assert 500
        processorResult.runEachIndexed { index ->
            value assert index
            inIdx assert index
            outIdx assert index
        }
    }
}