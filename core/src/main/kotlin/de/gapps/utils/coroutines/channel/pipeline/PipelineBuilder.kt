package de.gapps.utils.coroutines.channel.pipeline

data class PipelineBuilder<out P : Any, out I : Any, out O : Any, C : Any>(
    val producer: IProducer<@UnsafeVariance P>,
    val processor: IProcessor<I, O>,
    val processors: List<IProcessor<*, *>> = emptyList(),
    val consumer: IConsumer<C>? = null
) {
    suspend fun build(
        factory: PipelineBuilder<P, I, O, C>.(
            params: IProcessingParams, processors: List<IProcessor<*, *>>
        ) -> Pipeline<@UnsafeVariance P, C>
    ): Pipeline<P, C> = consumer?.let {
        factory(
            producer.params, processors
        ).apply {
            producer.pipeline = this@apply
            consumer.pipeline = this@apply
            consumer.run {
                Log.v("waiting for consumer job to join")
                producer.produce().process().consume().join()
                Log.v("consumer job joined")
            }
        }
    } ?: throw IllegalArgumentException("Can not build Pipeline without a consumer.")
}

operator fun <P : Any, I : Any, O : Any> IProducer<P>.plus(processor: IProcessor<I, O>): PipelineBuilder<P, I, O, Any> =
    PipelineBuilder(this, processor)

operator fun <P : Any, I : Any, O : Any> PipelineBuilder<P, Any, I, Any>.plus(p: IProcessor<I, O>): PipelineBuilder<P, I, O, Any> =
    PipelineBuilder(producer, p, processors + processor)

suspend operator fun <P : Any, I : Any> PipelineBuilder<P, Any, I, I>.plus(consumer: IConsumer<I>): Pipeline<P, I> {
    val factory: PipelineBuilder<P, Any, I, I>.(
        params: IProcessingParams, processors: List<IProcessor<*, *>>
    ) -> Pipeline<@UnsafeVariance P, I> = { params, processors -> Pipeline(params, processors = processors) }
    return this + (consumer to factory)
}

suspend operator fun <P : Any, I : Any> PipelineBuilder<P, Any, I, I>.plus(
    pair: Pair<IConsumer<I>, PipelineBuilder<P, Any, I, I>.(
        params: IProcessingParams, processors: List<IProcessor<*, *>>
    ) -> Pipeline<@UnsafeVariance P, I>>
): Pipeline<P, I> = PipelineBuilder(producer, processor, processors + processor, pair.first).build(pair.second)

operator fun <P : Any, I : Any> IConsumer<*>.rem(
    factory: PipelineBuilder<P, Any, I, I>.(
        params: IProcessingParams, processors: List<IProcessor<*, *>>
    ) -> Pipeline<@UnsafeVariance P, I>
) = this to factory

suspend operator fun <T : Any> IProducer<T>.plus(consumer: IConsumer<T>) =
    consumer.run { produce().consume().join() }