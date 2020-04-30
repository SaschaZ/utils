package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.pipeline.ProcessingElementStage.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface IConsumer<out I : Any> : IProcessingUnit<I, Any> {

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<Any>> {
        consume()
        return Channel()
    }

    override suspend fun IProcessingScope<@UnsafeVariance I, Any>.processSingle(value: @UnsafeVariance I) = Unit

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.consume() = scope.launchEx {
        var prevValue: IPipeValue<I>? = null
        tick(RECEIVE_INPUT)
        for (value in this@consume) {
            tick(CONSUMING)
            ConsumerScope(value, params, scope).consumeSingle(value.value)
            prevValue = value
            tick(RECEIVE_INPUT)
        }
        Log.v("consuming finished")
        tick(FINISHED_CONSUMING)
        prevValue?.let { pv -> ConsumerScope(pv, params, scope).onConsumingFinished() }
        tick(FINISHED_CLOSING)
    }

    suspend fun IConsumerScope<@UnsafeVariance I>.consumeSingle(value: @UnsafeVariance I)

    suspend fun IConsumerScope<@UnsafeVariance I>.onConsumingFinished() = Unit
}

fun <I : Any> consumer(
    params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Consumer"),
    block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
): IConsumer<I> = Consumer(params, identity, block)

fun <I : Any> IParamsHolder.consumer(
    identity: Identity = Id("Consumer"),
    block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
): IConsumer<I> = Consumer(params, identity, block)

open class Consumer<out I : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Consumer"),
    private val block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
) : AbsProcessingUnit<I, Any>(), IConsumer<I>, Identity by identity {

    override val isConsumer: Boolean get() = true

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override val outputChannel: Channel<out IPipeValue<Any>>
        get() = throw IllegalStateException("Consumer do not have an output channel.")

    override suspend fun IConsumerScope<@UnsafeVariance I>.consumeSingle(value: @UnsafeVariance I) = block(value)
}

