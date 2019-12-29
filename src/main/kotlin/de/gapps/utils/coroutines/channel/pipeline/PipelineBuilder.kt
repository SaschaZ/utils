package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IConsumer
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.channel.IProducer

data class PipelineBuilderScope<I : Any>(
    val producer: IProducer<I>,
    val processors: List<IProcessor<*, *>>
) {
    constructor(
        producer: IProducer<I>,
        processor: IProcessor<*, *>
    ) : this(producer, listOf(processor))

    init {
        producer.outputType == processors.firstOrNull()?.inputType
    }
}

operator fun <I : Any> IProducer<I>.plus(processor: IProcessor<*, *>) = PipelineBuilderScope(this, processor)
suspend inline operator fun <reified I : Any, reified O : Any> IProducer<I>.plus(consumer: IConsumer<O>) =
    Pipeline(params, I::class, O::class, emptyList()).run {
        pipeline = this
        consumer.pipeline = this
        consumer.run {
            produce().process().consume().join()
        }
    }

operator fun <I : Any> PipelineBuilderScope<I>.plus(processor: IProcessor<*, *>) =
    PipelineBuilderScope(producer, listOf(*this@plus.processors.toTypedArray(), processor))

suspend inline operator fun <reified I : Any, reified O : Any> PipelineBuilderScope<I>.plus(consumer: IConsumer<O>) =
    Pipeline(producer.params, I::class, O::class, processors).run {
        producer.pipeline = this
        consumer.pipeline = this
        consumer.run {
            producer.produce().process().consume().join()
        }
    }