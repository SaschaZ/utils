package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessorTest : AnnotationSpec() {

    private val testProducer = Producer<Int> {
        repeat(5) { send(it) }
        close()
    }

    @Test
    fun testProcessor() = runBlocking {
        var finished = false
        val processorResult = Processor<Int, Int> {
            send(it)
        }.run {
            onProcessingFinished = { finished = true }
            testProducer.produce().process().toList()
        }

        assertTrue(finished)
        assertEquals(5, processorResult.size)
        processorResult.forEachIndexed { index, i ->
            assertEquals(index, i.value)
        }
    }.asUnit()
}