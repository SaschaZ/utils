package de.gapps.utils.coroutines.channel.network

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.reflect.KClass

sealed class Port<out T : Any> : Identifiable {

    companion object {

        const val NO_PARALLEL_PROCESSING = 1
    }

    abstract val type: KClass<@UnsafeVariance T>
    abstract val channelCapacity: Int
    internal abstract val internalChannel: Channel<INodeValue<@UnsafeVariance T>>

    abstract val isInput: Boolean
    abstract val isOutput: Boolean
    abstract val isIoPut: Boolean

    data class Input<out T : Any> internal constructor(
        override val type: KClass<@UnsafeVariance T>,
        override val id: String,
        override val channelCapacity: Int = Channel.BUFFERED,
        override val isInput: Boolean = true,
        override val isOutput: Boolean = false,
        override val isIoPut: Boolean = false,
        override val internalChannel: Channel<INodeValue<@UnsafeVariance T>> = Channel(channelCapacity)
    ) : Port<T>(), ReceiveChannel<INodeValue<T>> by internalChannel

    data class Output<out T : Any> internal constructor(
        override val type: KClass<@UnsafeVariance T>,
        override val id: String,
        override val channelCapacity: Int = Channel.BUFFERED,
        override val isInput: Boolean = false,
        override val isOutput: Boolean = true,
        override val isIoPut: Boolean = false,
        override val internalChannel: Channel<INodeValue<@UnsafeVariance T>> = Channel(channelCapacity)
    ) : Port<T>(), SendChannel<INodeValue<@UnsafeVariance T>> by internalChannel

    data class IoPut<out T : Any> internal constructor(
        override val type: KClass<@UnsafeVariance T>,
        override val id: String,
        override val channelCapacity: Int = Channel.BUFFERED,
        override val isInput: Boolean = false,
        override val isOutput: Boolean = false,
        override val isIoPut: Boolean = true,
        override val internalChannel: Channel<INodeValue<@UnsafeVariance T>> = Channel(channelCapacity)
    ) : Port<T>(), Channel<INodeValue<@UnsafeVariance T>> by internalChannel
}

@Suppress("FunctionName")
fun <T : Any> Input(
    type: KClass<T>,
    id: String,
    channelCapacity: Int = Channel.BUFFERED,
    link: Channel<INodeValue<T>>? = null
) = Port.Input(type, id, channelCapacity, internalChannel = link ?: Channel(channelCapacity))

@Suppress("FunctionName")
inline fun <reified T : Any> Input(
    id: String,
    channelCapacity: Int = Channel.BUFFERED,
    link: Channel<INodeValue<T>>? = null
) = Input(T::class, id, channelCapacity, link ?: Channel(channelCapacity))


@Suppress("FunctionName")
fun <T : Any> Output(
    type: KClass<T>,
    id: String,
    channelCapacity: Int = Channel.BUFFERED,
    link: Channel<INodeValue<T>>? = null
) = Port.Output(type, id, channelCapacity, internalChannel = link ?: Channel(channelCapacity))

@Suppress("FunctionName")
inline fun <reified T : Any> Output(
    id: String,
    channelCapacity: Int = Channel.BUFFERED,
    link: Channel<INodeValue<T>>? = null
) = Output(T::class, id, channelCapacity, link ?: Channel(channelCapacity))


@Suppress("FunctionName")
fun <T : Any> IoPut(
    type: KClass<T>,
    id: String,
    channelCapacity: Int = Channel.BUFFERED,
    link: Channel<INodeValue<T>>? = null
) = Port.IoPut(type, id, channelCapacity, internalChannel = link ?: Channel(channelCapacity))

@Suppress("FunctionName")
inline fun <reified T : Any> IoPut(
    id: String,
    channelCapacity: Int = Channel.BUFFERED,
    link: Channel<INodeValue<T>>? = null
) = IoPut(T::class, id, channelCapacity, link ?: Channel(channelCapacity))


operator fun Port<*>.plus(other: Port<*>): IPortContainer {
    val inputs = ArrayList<Port.Input<*>>()
    val outputs = ArrayList<Port.Output<*>>()
    val ioPuts = ArrayList<Port.IoPut<*>>()

    if (isInput) inputs.add(this as Port.Input<*>)
    if (isOutput) outputs.add(this as Port.Output<*>)
    if (isIoPut) ioPuts.add(this as Port.IoPut<*>)

    if (other.isInput) inputs.add(other as Port.Input<*>)
    if (other.isOutput) outputs.add(other as Port.Output<*>)
    if (other.isIoPut) ioPuts.add(other as Port.IoPut<*>)

    return PortContainer(inputs, outputs, ioPuts)
}

operator fun IPortContainer.plus(other: Port<*>): IPortContainer {
    if (other.isInput) inputs[other.id] = other as Port.Input<*>
    if (other.isOutput) outputs[other.id] = other as Port.Output<*>
    if (other.isIoPut) ioPuts[other.id] = other as Port.IoPut<*>

    return PortContainer(inputs, outputs, ioPuts)
}