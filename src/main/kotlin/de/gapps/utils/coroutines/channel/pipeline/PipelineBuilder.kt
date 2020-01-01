package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.log.Log

data class PipelineBuilderScope<out I : Any>(
    val producer: IProducer<@UnsafeVariance I>,
    val processors: List<IProcessor<*, *>>
) {
    constructor(
        producer: IProducer<I>,
        processor: IProcessor<*, *>
    ) : this(producer, listOf(processor))
}

operator fun <I : Any> IProducer<I>.plus(processor: IProcessor<*, *>) = PipelineBuilderScope(this, processor)

suspend inline operator fun <I : Any, O : Any> IProducer<I>.plus(consumer: IConsumer<O>) =
    Pipeline<I, O>(params, emptyList()).run {
        pipeline = this
        consumer.pipeline = this
        consumer.run {
            produce().process().consume().run { Log.v("before pipe join"); join(); Log.v("after pipe join") }
        }
    }

operator fun <I : Any> PipelineBuilderScope<I>.plus(processor: IProcessor<*, *>) =
    PipelineBuilderScope(producer, listOf(*this@plus.processors.toTypedArray(), processor))

suspend operator fun <I : Any, O : Any> PipelineBuilderScope<I>.plus(consumer: IConsumer<O>) =
    Pipeline<I, O>(producer.params, processors).run {
        producer.pipeline = this
        consumer.pipeline = this
        consumer.run {
            producer.produce().process().consume().run { Log.v("before pipe join"); join(); Log.v("after pipe join") }
        }
    }