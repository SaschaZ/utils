package de.gapps.utils.coroutines.channel.pipeline

import io.kotlintest.specs.AnnotationSpec

class PipelineTest : AnnotationSpec() {

//    private val testProducer = Producer<String> {
//        repeat(5) { send("$it") }
//        close()
//    }
//
//    private val inputProcessor = Processor<String, Long> { send(it.toLong()) }
//
//    private val parallelProcessor0 = Processor<Long, Int> { send(it.toInt()) }
//    private val parallelProcessor1 = Processor<Long, Double> { send(it.toDouble()) }
//    private val parallelProcessor2 = Processor<Long, String> { send(it.toString()) }
//
//    private val parallelTestProcessor = ParallelProcessor(
//        ParallelProcessingParams(
//            ParallelProcessingTypes.UNIQUE
//        )
//    ) {
//        when (it) {
//            0 -> parallelProcessor0
//            1 -> parallelProcessor1
//            else -> parallelProcessor2
//        }
//    }
//
//    private fun groupByIdx() = Processor<Any, Pair<Int, Any>> {
//
//    }
//
//    private val outputProcessor = Processor<Pair<Int, Any>, String> { send(it.toString()) }
//
//    @Test
//    fun testPipeline() = runBlocking {
//        val consumerResult = ArrayList<Int>()
//        val testConsumer = Consumer<Int> { consumerResult.add(it); Log.v("consume: $it") }
//        testProducer + inputProcessor + parallelTestProcessor + groupByIdx() + outputProcessor + testConsumer
//        assertEquals(5, consumerResult.size)
//        consumerResult.sorted().forEachIndexed { index, i ->
//            assertEquals(index, i)
//        }
//    }.asUnit()
}