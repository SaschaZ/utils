@file:Suppress("FunctionName")

package de.gapps.utils.coroutines.channel.pipeline

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

interface IProducer<out T : Any> : IProcessingUnit<Any, T> {

    override fun ReceiveChannel<IPipeValue<Any>>.process(): ReceiveChannel<IPipeValue<T>> = produce()
    override suspend fun IProcessingScope<Any, @UnsafeVariance T>.processSingle(value: Any) = Unit

    fun produce(): ReceiveChannel<IPipeValue<@UnsafeVariance T>>

    override suspend fun IProducerScope<@UnsafeVariance T>.onProcessingFinished() = onProducingFinished()

    suspend fun IProducerScope<@UnsafeVariance T>.onProducingFinished() = Unit
}

inline fun <reified T : Any> producer(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<@UnsafeVariance T>> = Channel(params.channelCapacity),
    identity: Identity = Id("Producer"),
    noinline block: suspend IProducerScope<@UnsafeVariance T>.() -> Unit =
        { throw IllegalArgumentException("No producer block defined") }
): IProducer<T> = Producer(params, outputChannel, identity, T::class, block)

inline fun <reified T : Any> IParamsHolder.producer(
    outputChannel: Channel<IPipeValue<@UnsafeVariance T>> = Channel(params.channelCapacity),
    identity: Identity = Id("Producer"),
    noinline block: suspend IProducerScope<@UnsafeVariance T>.() -> Unit =
        { throw IllegalArgumentException("No producer block defined") }
): IProducer<T> = Producer(params, outputChannel, identity, T::class, block)

inline fun <reified T : Any, reified R : Any> checkGenerics(block: () -> R): R {
    if (T::class.typeParameters.isNotEmpty())
        throw IllegalArgumentException("Generics can not have generics itself: ${T::class}")
    return block()
}

open class Producer<out T : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    override val outputChannel: Channel<IPipeValue<@UnsafeVariance T>> = Channel(params.channelCapacity),
    identity: Identity = Id("Producer"),
    outputType: KClass<T>,
    val produce: suspend IProducerScope<@UnsafeVariance T>.() -> Unit =
        { throw IllegalArgumentException("No producer block defined") }
) : AbsProcessingUnit<Any, T>(Any::class, outputType), IProducer<T>, Identity by identity {

    override fun produce(): ReceiveChannel<IPipeValue<T>> = scope.launch {
        pipeline.tick(this@Producer, PipelineElementStage.PROCESSING)
        producerScope().produce()
    }.let { outputChannel }
}
