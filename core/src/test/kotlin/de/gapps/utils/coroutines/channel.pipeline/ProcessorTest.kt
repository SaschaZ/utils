package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.misc.asUnit
import de.gapps.utils.misc.runEachIndexed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ProcessorTest : IParamsHolder {

    override val params: IProcessingParams = ProcessingParams()

    private val testProducer = producer<Int> {
        repeat(5) { send(it) }
        close()
    }

    @Test
    fun testProcessor() = runBlocking {
        var finished = false
        val processorResult = object : Processor<Int, Int>(params, block = {
            send(it)
        }, identity = NoId, outputChannel = Channel()) {
            override suspend fun IProducerScope<Int>.onProcessingFinished() {
                finished = true
            }
        }.run {
            testProducer.produce().process().toList()
        }
        finished assert true
        processorResult.size assert 5
        processorResult.runEachIndexed { index ->
            value assert index
            inIdx assert index
            outIdx assert index
        }
    }.asUnit()
}