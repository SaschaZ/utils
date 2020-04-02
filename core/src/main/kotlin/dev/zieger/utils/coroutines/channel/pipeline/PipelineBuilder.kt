package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.misc.catch

data class PipelineBuilderScope<out I : Any>(
    val producer: IProducer<@UnsafeVariance I>,
    val processors: List<IProcessor<*, *>>
) {
    constructor(
        producer: IProducer<I>,
        processor: IProcessor<*, *>
    ) : this(producer, listOf(processor))
}

operator fun <I : Any> IProducer<I>.plus(processor: IProcessor<*, *>): PipelineBuilderScope<I> {
    catch(Unit, onCatch = {
        throw IllegalArgumentException("Input type of given processor does not match output type of producer. ($it)")
    }) {
        @Suppress("UNCHECKED_CAST")
        processor as IProcessor<I, *>
    }

    return PipelineBuilderScope(this, processor)
}

suspend operator fun <T : Any> IProducer<T>.plus(consumer: IConsumer<T>) =
    consumer.run { produce().consume().join() }

operator fun <I : Any, T : Any> PipelineBuilderScope<I>.plus(processor: IProcessor<T, *>): PipelineBuilderScope<I> {
    catch(Unit, onCatch = {
        throw IllegalArgumentException("Input type of given processor does not match output type of last processor in pipeline. ($it)")
    }) {
        @Suppress("UNCHECKED_CAST")
        processors.last() as IProcessor<*, T>
    }

    return PipelineBuilderScope(producer, listOf(*this@plus.processors.toTypedArray(), processor))
}

suspend operator fun <I : Any, O : Any> PipelineBuilderScope<I>.plus(consumer: IConsumer<O>) {
    catch(Unit, onCatch = {
        throw IllegalArgumentException("Input type of given consumer does not match output type of last processor in pipeline. ($it)")
    }) {
        @Suppress("UNCHECKED_CAST")
        processors.last() as IProcessor<*, O>
    }

    return Pipeline<I, O>(producer.params, pipes = processors).run {
        producer.pipeline = this
        consumer.pipeline = this
        consumer.run {
            Log.v("waiting for consumer job to join")
            producer.produce().process().consume().join()
            Log.v("consumer job joined")
        }
    }
}