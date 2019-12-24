package de.gapps.utils.coroutines.channel

import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class ProcessorTest : AnnotationSpec() {

    private val testProducer = producer<Int> {
        repeat(5) { send(it) }
        close()
    }

    @Test
    fun testProcessor() = runBlocking {
        val processorResult = processor<Int, Int> { send(it) }.run {
            testProducer.produce().process().toList()
        }
        assertEquals(5, processorResult.size)
        processorResult.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}