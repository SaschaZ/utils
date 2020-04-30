@file:Suppress("FunctionName")

package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.channel.pipeline.ProcessingElementStage.PRODUCING
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

interface IProducer<out T : Any> : IProcessingUnit<Any, T> {

    override fun ReceiveChannel<IPipeValue<Any>>.process(): ReceiveChannel<IPipeValue<T>> = produce()
    override suspend fun IProcessingScope<Any, @UnsafeVariance T>.processSingle(value: Any) = Unit

    fun produce(): ReceiveChannel<IPipeValue<@UnsafeVariance T>>

    override suspend fun IProducerScope<@UnsafeVariance T>.onProcessingFinished() = onProducingFinished()

    suspend fun IProducerScope<@UnsafeVariance T>.onProducingFinished() = Unit
}

fun <T : Any> producer(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<@UnsafeVariance T>> = Channel(params.channelCapacity),
    identity: Identity = Id("Producer"),
    block: suspend IProducerScope<@UnsafeVariance T>.() -> Unit =
        { throw IllegalArgumentException("No producer block defined") }
): IProducer<T> = Producer(params, outputChannel, identity, block)

fun <T : Any> IParamsHolder.producer(
    outputChannel: Channel<IPipeValue<@UnsafeVariance T>> = Channel(params.channelCapacity),
    identity: Identity = Id("Producer"),
    block: suspend IProducerScope<@UnsafeVariance T>.() -> Unit =
        { throw IllegalArgumentException("No producer block defined") }
): IProducer<T> = Producer(params, outputChannel, identity, block)

open class Producer<out T : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    override val outputChannel: Channel<IPipeValue<@UnsafeVariance T>> = Channel(params.channelCapacity),
    identity: Identity = Id("Producer"),
    val produce: suspend IProducerScope<@UnsafeVariance T>.() -> Unit =
        { throw IllegalArgumentException("No producer block defined") }
) : AbsProcessingUnit<Any, T>(), IProducer<T>, Identity by identity {

    override val isProducer: Boolean get() = true

    override fun produce(): ReceiveChannel<IPipeValue<T>> = scope.launch {
        tick(PRODUCING)
        producerScope().produce()
    }.let { outputChannel }
}
