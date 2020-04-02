//package dev.zieger.utils.coroutines.channel.pipeline
//
//fun main(args: Array<String>) {
//    pipeline<Any, String> {
//
//        producer<I> {
//        }
//
//        parallel<I, O> {
//            processor<I, O> {
//
//            }
//        }
//
//        consumer<O> {
//
//        }
//    }
//}
//
//class PipelineScope<I: Any, O: Any>
//
//inline fun <reified I : Any, reified O : Any> pipeline(block: PipelineScope<I, O>.() -> Unit) {
//    PipelineScope<I, O>().block()
//}
//
//data class ProducerScope(private val pipelineScope: PipelineScope)
//
//inline fun <reified O: Any> PipelineScope.producer(block: ProducerScope.() -> Unit) {
//    ProducerScope(this).block()
//}
//
//data class ProcessorScope(private val pipelineScope: PipelineScope)
//
//inline fun <reified I: Any, reified O: Any> PipelineScope.processor(block: ProcessorScope.() -> Unit) {
//    ProcessorScope(this).block()
//}
//
//data class ConsumerScope(private val pipelineScope: PipelineScope)
//
//inline fun <reified I: Any> PipelineScope.consumer(block: ConsumerScope.() -> Unit) {
//    ConsumerScope(this).block()
//}