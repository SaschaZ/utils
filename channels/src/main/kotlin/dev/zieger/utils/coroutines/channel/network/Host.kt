package dev.zieger.utils.coroutines.channel.network

import dev.zieger.utils.coroutines.channel.network.Identifiable.Companion.NO_ID
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex

interface IHost : INode {

    val nodes: List<INode>

    fun addNode(node: INode)
    fun linkPorts(ports: Pair<String, String>)

    override suspend fun ProcessingScope.process() = Unit // unused in Host
}

inline fun <reified T : Any> IHost.sendScopeForId(id: String, block: SendScope<T>.() -> Unit) =
    SendScope(inputForId<T>(id)).block()

inline fun <reified T : Any> IHost.receiveScopeForId(id: String, block: ReceiveScope<T>.() -> Unit) =
    ReceiveScope(outputForId<T>(id)).block()

inline fun <reified T : Any> IHost.ioScopeForId(id: String, block: IoScope<T>.() -> Unit) =
    IoScope(ioPutForId<T>(id)).block()

class SendScope<out T : Any>(private val port: Port.Input<T>) :
    SendChannel<INodeValue<@UnsafeVariance T>> by port.internalChannel

class ReceiveScope<out T : Any>(private val port: Port.Output<T>) :
    ReceiveChannel<INodeValue<@UnsafeVariance T>> by port.internalChannel

class IoScope<out T : Any>(private val port: Port.IoPut<T>) :
    Channel<INodeValue<@UnsafeVariance T>> by port.internalChannel

open class Host(
    override val ports: IPortContainer,
    override val id: String,
    override val nodes: ArrayList<INode> = ArrayList(),
    override val scope: CoroutineScope = DefaultCoroutineScope(),
    override val mutex: Mutex? = null
) : IHost {

    @Suppress("LeakingThis")
    override var host: IHost = this

    override fun addNode(node: INode) {
        nodes.add(node)
        node.host = this
    }

    override fun linkPorts(ports: Pair<String, String>) {
        val portId0 = ports.first
        val portId1 = ports.second
        if (portId0 == NO_ID || portId1 == NO_ID) throw IllegalArgumentException("Can not find port with id NO_ID")
        val p0 = portForId(portId0)
            ?: nodes.find { it.ports.portForId(portId0) != null }?.portForId(portId0)
            ?: throw IllegalArgumentException("Can not find port with id $portId0")
        val p1 = portForId(portId1)
            ?: nodes.find { it.ports.portForId(portId1) != null }?.portForId(portId1)
            ?: throw IllegalArgumentException("Can not find port with id $portId1")
        p0 link p1
    }
}