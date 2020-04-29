package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.pipeline.ProcessingElementStage.*
import kotlinx.coroutines.Job
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
        tick(this@IConsumer, RECEIVE_INPUT)
        for (value in this@consume) {
            tick(this@IConsumer, PROCESSING)
            ConsumerScope(value, params, scope).consumeSingle(value.value)
            prevValue = value
            tick(this@IConsumer, RECEIVE_INPUT)
        }
        Log.v("consuming finished")
        tick(this@IConsumer, FINISHED_PROCESSING)
        prevValue?.let { pv -> ConsumerScope(pv, params, scope).onConsumingFinished() }
        tick(this@IConsumer, FINISHED_CLOSING)
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

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override val outputChannel: Channel<out IPipeValue<Any>>
        get() = throw IllegalStateException("Consumer do not have an output channel.")

    override suspend fun IConsumerScope<@UnsafeVariance I>.consumeSingle(value: @UnsafeVariance I) = block(value)
}

fun <I : Any> listConsumer(
    params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Consumer"),
    block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
): IConsumer<List<I>> = ListConsumer(params, identity, block)

fun <I : Any> IParamsHolder.listConsumer(
    identity: Identity = Id("Consumer"),
    block: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No consumer block defined") }
): IConsumer<List<I>> = ListConsumer(params, identity, block)


open class ListConsumer<out I : Any>(
    params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Consumer"),
    private val block2: suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit =
        throw IllegalArgumentException("Block can not be empty.")
) : Consumer<List<I>>(params, identity) {

    override fun ReceiveChannel<IPipeValue<List<@UnsafeVariance I>>>.consume(): Job = scope.launchEx {
        for (value in this@consume) {
            value.value.forEach { v ->
                ConsumerScope(
                    PipeValue(v, value.time, value.outIdx, outIdx++, value.parallelIdx, value.parallelType),
                    params, scope
                ).block2(v)
            }
        }
    }
}

