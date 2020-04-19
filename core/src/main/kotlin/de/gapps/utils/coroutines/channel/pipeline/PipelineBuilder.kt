package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.parallel.ParallelProcessor
import de.gapps.utils.misc.catch
import kotlinx.coroutines.channels.Channel

data class PipelineBuilderScope<out I : Any>(
    val producer: IProducer<@UnsafeVariance I>,
    val processors: List<IProcessingUnit<*, *>>
) {
    constructor(
        producer: IProducer<I>,
        processor: IProcessingUnit<*, *>
    ) : this(producer, listOf(processor))
}

operator fun <I : Any> IProducer<I>.plus(processor: IProcessingUnit<*, *>): PipelineBuilderScope<I> {
    catch(Unit, onCatch = {
        throw IllegalArgumentException("Input type of given processor does not match output type of producer. ($it)")
    }) {
        @Suppress("UNCHECKED_CAST")
        processor as IProcessingUnit<I, *>
    }

    return PipelineBuilderScope(this, processor)
}

suspend operator fun <T : Any> IProducer<T>.plus(consumer: IConsumer<T>) =
    consumer.run { produce().consume().join() }

operator fun <I : Any, T : Any> PipelineBuilderScope<I>.plus(processor: IProcessingUnit<T, *>): PipelineBuilderScope<I> {
    catch(Unit, onCatch = {
        throw IllegalArgumentException("Input type of given processor does not match output type of last processor in pipeline. ($it)")
    }) {
        @Suppress("UNCHECKED_CAST")
        processors.last() as IProcessingUnit<*, T>
    }

    return PipelineBuilderScope(producer, listOf(*this@plus.processors.toTypedArray(), processor))
}

inline operator fun <reified I : Any, reified O : Any> IProducer<I>.times(
    noinline processorFactory: (idx: Int) -> IProcessingUnit<I, O>
): PipelineBuilderScope<I> {
    return this + ParallelProcessor(
        params, processorFactory = processorFactory,
        inputType = I::class, outputType = O::class
    )
}

inline operator fun <reified I : Any, reified O : Any> PipelineBuilderScope<I>.times(
    noinline processorFactory: (idx: Int) -> IProcessingUnit<I, O>
): PipelineBuilderScope<I> {
    return this + ParallelProcessor(
        producer.params, processorFactory = processorFactory,
        inputType = I::class, outputType = O::class
    )
}

operator fun <A : Any?, B : Any?> A.div(other: B): Pair<A, B> = this to other

suspend inline operator fun <reified I : Any, reified O : Any> PipelineBuilderScope<I>.plus(consumer: IConsumer<O>): IPipeline<I, O> {
    val block: PipelineFactoryScope<I, O>.() -> Unit = {}
    return this + consumer / block
}

suspend inline operator fun <reified I : Any, reified O : Any> PipelineBuilderScope<I>.plus(pair: Pair<IConsumer<O>, PipelineFactoryScope<I, O>.() -> Unit>): IPipeline<I, O> {
    catch(Unit, onCatch = {
        throw IllegalArgumentException("Input type of given consumer does not match output type of last processor in pipeline. ($it)")
    }) {
        @Suppress("UNCHECKED_CAST")
        processors.last() as IProcessingUnit<*, O>
    }
    val (consumer, block) = pair

    return PipelineFactoryScope<I, O>().apply(block).run {
        Pipeline<I, O>(
            producer.params, processors = processors,
            inputType = I::class, outputType = O::class
        ).apply {
            producer.pipeline = this@apply
            consumer.pipeline = this@apply
            consumer.run {
                Log.v("waiting for consumer job to join")
                producer.produce().process().consume().join()
                Log.v("consumer job joined")
            }
        }
    }
}

operator fun <I : Any, O : Any> IConsumer<O>.rem(block: IPipeline<I, O>.() -> Unit): Pair<IConsumer<O>, IPipeline<I, O>.() -> Unit> =
    this to block

data class PipelineFactoryScope<I : Any, out O : Any>(
    var params: IProcessingParams = ProcessingParams(),
    var identity: Identity = Id("Pipeline"),
    var outputChannel: Channel<out IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    var pipes: List<IProcessingUnit<*, *>> = emptyList(),
    var watchDogObserving: Boolean = false
)