package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IConsumer
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.channel.IProducer

data class ProducerProcessorScope<I>(
    val producer: IProducer<I>,
    val processor: IProcessor<*, *>
)

operator fun <I> IProducer<I>.plus(processor: IProcessor<*, *>) = ProducerProcessorScope(this, processor)
suspend operator fun <I, O> IProducer<I>.plus(consumer: IConsumer<O>) = Pipeline<I, O>(params, emptyList()).run {
    pipeline = this
    consumer.pipeline = this
    consumer.run {
        produce().process().consume().join()
    }
}

data class ProcessorScope<I>(
    val producer: IProducer<I>,
    val processors: List<IProcessor<*, *>>
)

operator fun <I> ProducerProcessorScope<I>.plus(processor: IProcessor<*, *>) =
    ProcessorScope(producer, listOf(this@plus.processor, processor))

operator fun <I> ProcessorScope<I>.plus(processor: IProcessor<*, *>) =
    ProcessorScope(producer, listOf(*this@plus.processors.toTypedArray(), processor))

suspend operator fun <I, O> ProducerProcessorScope<I>.plus(consumer: IConsumer<O>) =
    producer + consumer

suspend operator fun <I, O> ProcessorScope<I>.plus(consumer: IConsumer<O>) =
    Pipeline<I, O>(producer.params, processors).run {
        producer.pipeline = this
        consumer.pipeline = this
        consumer.run {
            producer.produce().process().consume().join()
        }
    }