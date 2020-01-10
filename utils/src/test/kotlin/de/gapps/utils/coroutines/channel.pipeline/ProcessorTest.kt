package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.equals
import de.gapps.utils.misc.asUnit
import de.gapps.utils.misc.runEachIndexed
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.assertTrue

class ProcessorTest : AnnotationSpec() {

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

        assertTrue(finished)
        processorResult.size equals 5
        processorResult.runEachIndexed { index ->
            value equals index
            inIdx equals index
            outIdx equals index
        }
    }.asUnit()
}