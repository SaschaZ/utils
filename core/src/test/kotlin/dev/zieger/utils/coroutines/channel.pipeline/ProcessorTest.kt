package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEachIndexed
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ProcessorTest {

    private val testProducer = Producer<Int> {
        repeat(5) { send(it) }
        close()
    }

    @Test
    fun testProcessor() = runBlocking {
        var finished = false
        val processorResult = object : Processor<Int, Int>(ProcessingParams(), block = {
            send(it)
        }) {
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