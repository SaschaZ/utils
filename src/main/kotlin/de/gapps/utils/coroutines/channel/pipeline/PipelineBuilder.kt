package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IConsumer
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.channel.IProducer

data class ProducerProcessorScope(
    val producer: IProducer<*>,
    val processor: IProcessor<*, *>
)

operator fun IProducer<*>.plus(processor: IProcessor<*, *>) = ProducerProcessorScope(this, processor)

data class ProcessorScope(
    val producer: IProducer<*>,
    val processors: List<IProcessor<*, *>>
)

operator fun ProducerProcessorScope.plus(processor: IProcessor<*, *>) =
    ProcessorScope(producer, listOf(this@plus.processor, processor))

operator fun ProcessorScope.plus(processor: IProcessor<*, *>) =
    ProcessorScope(producer, listOf(*this@plus.processors.toTypedArray(), processor))

operator fun ProcessorScope.plus(consumer: IConsumer<*>) = Pipeline(producer, processors, consumer).start()