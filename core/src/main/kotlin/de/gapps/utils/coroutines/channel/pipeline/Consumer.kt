package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.pipeline.PipelineElementStage.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass

interface IConsumer<out I : Any> : IProcessingUnit<I, Any> {

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<Any>> {
        consume()
        return Channel()
    }

    override suspend fun IProcessingScope<@UnsafeVariance I, Any>.processSingle(value: @UnsafeVariance I) = Unit

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.consume() = scope.launchEx {
        var prevValue: IPipeValue<I>? = null
        pipeline.tick(this@IConsumer, RECEIVE_INPUT)
        for (value in this@consume) {
            pipeline.tick(this@IConsumer, PROCESSING)
            ConsumerScope(value, params).consumeSingle(value.value)
            prevValue = value
            pipeline.tick(this@IConsumer, RECEIVE_INPUT)
        }
        Log.v("consuming finished")
        pipeline.tick(this@IConsumer, FINISHED_PROCESSING)
        prevValue?.let { pv -> ConsumerScope(pv, params).onConsumingFinished() }
        pipeline.tick(this@IConsumer, FINISHED_CLOSING)
    }

    suspend fun IConsumerScope<@UnsafeVariance I>.consumeSingle(value: @UnsafeVariance I)

    suspend fun IConsumerScope<@UnsafeVariance I>.onConsumingFinished() = Unit
}

inline fun <reified I : Any> consumer(
    params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Consumer"),
    noinline block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
): Consumer<I> = checkGenerics<I, Consumer<I>> { Consumer(params, identity, I::class, block) }

inline fun <reified I : Any> IParamsHolder.consumer(
    identity: Identity = Id("Consumer"),
    noinline block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
): Consumer<I> = checkGenerics<I, Consumer<I>> { Consumer(params, identity, I::class, block) }

open class Consumer<out I : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Consumer"),
    inputType: KClass<I>,
    private val block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
) : AbsProcessingUnit<I, Any>(inputType, Any::class), IConsumer<I>, Identity by identity {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override val outputChannel: Channel<out IPipeValue<Any>> = Channel()

    override suspend fun IConsumerScope<@UnsafeVariance I>.consumeSingle(value: @UnsafeVariance I) = block(value)
}

