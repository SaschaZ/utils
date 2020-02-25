package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.misc.asUnit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.random.Random

class NetworkTest : AnnotationSpec() {

    @Before
    fun before() = runBlocking {

    }.asUnit()

    @After
    fun after() = runBlocking {

    }.asUnit()

    @Ignore
    @Test
    fun testNetwork() = runBlocking {
        withTimeout(100L) {
            lateinit var host: IHost
            network {
                host = host {
                    id = "Host0"
                    val host0Input0 = input<Int> {
                        id = "Input0"
                    }
                    val host0Output0 = output<Int> {
                        id = "Output0"
                    }

                    lateinit var node0Output0: Port.Output<Int>
                    lateinit var node1Input0: Port.Input<Int>

                    node {
                        id = "Node0"

                        host0Input0 link input<Int> {
                            id = "Input0"
                        }
                        node0Output0 = output {
                            id = "Output0"
                        }

                        process {
                            @Suppress("UNCHECKED_CAST")
                            (node.outputForId<Int>("Output0").send(
                                inputs.values.mapNotNull { it.poll() }.first() as INodeValue<Int>
                            ))
                        }
                    }

                    node {
                        id = "Node1"

                        node1Input0 = input {
                            id = "Input0"
                        }
                        output<Int> {
                            id = "Output0"
                        } link host0Output0

                        process {
                            @Suppress("UNCHECKED_CAST")
                            node.outputForId<Int>("Output0").send(
                                inputs.mapNotNull { it.value.poll() }.first() as INodeValue<Int>
                            )
                        }
                    }

                    node0Output0 link node1Input0
                }
            }

            host.sendScopeForId<Int>("Input0") {
                send(NodeValue(1337))
                close()
            }
            host.receiveScopeForId<Int>("Output0") {
                for (value in this) {
                    println("${value.value}")
                }
            }
        }
    }.asUnit()
}

class IoPutScope<out T : Any>(val portHolderScope: PortHolderScope) {

    var id: String = Random(System.currentTimeMillis()).nextLong().toString()
    var link: Channel<INodeValue<@UnsafeVariance T>>? = null
}

inline fun <reified T : Any> IoPutScope<T>.createPort(): Port.IoPut<T> =
    IoPut(id, link = link).also { portHolderScope.addIoPut(it) }

class OutputScope<out T : Any>(val portHolderScope: PortHolderScope) {

    var id: String = Random(System.currentTimeMillis()).nextLong().toString()
    var link: Channel<INodeValue<@UnsafeVariance T>>? = null
}

inline fun <reified T : Any> OutputScope<T>.createPort(): Port.Output<T> =
    Output(id, link = link).also { portHolderScope.addOutput(it) }

class InputScope<out T : Any>(val portHolderScope: PortHolderScope) {

    var id: String = Random(System.currentTimeMillis()).nextLong().toString()
    var link: Channel<INodeValue<@UnsafeVariance T>>? = null
}

inline fun <reified T : Any> InputScope<T>.createPort(): Port.Input<T> =
    Input(id, link = link).also { portHolderScope.addInput(it) }

fun HostScope.node(block: NodeScope.() -> Unit): Node =
    NodeScope(this).apply(block).createNode().also { addNode(it) }

inline fun <reified T : Any> PortHolderScope.input(block: InputScope<T>.() -> Unit): Port.Input<T> =
    InputScope<T>(this).apply(block).createPort().also { addInput(it) }

inline fun <reified T : Any> PortHolderScope.output(block: OutputScope<T>.() -> Unit): Port.Output<T> =
    OutputScope<T>(this).apply(block).createPort().also { addOutput(it) }

inline fun <reified T : Any> PortHolderScope.ioPut(block: IoPutScope<T>.() -> Unit): Port.IoPut<T> =
    IoPutScope<T>(this).apply(block).createPort().also { addIoPut(it) }

fun NodeScope.process(
    block: suspend ProcessingScope.() -> Unit
) {
    process = block
}

class NodeScope(val hostScope: HostScope) : PortHolderScope() {

    var id: String? = null

    internal var process: (suspend ProcessingScope.() -> Unit)? = null

    fun createNode() = Node(
        PortContainer(inputs, outputs, ioPuts),
        id ?: throw IllegalArgumentException("No id provided"),
        p = process ?: throw IllegalArgumentException("No process provided")
    )
}

fun NetworkScope.host(block: HostScope.() -> Unit): Host =
    HostScope(this).apply(block).createHost().also { addHost(it) }

class HostScope(networkScope: NetworkScope) : PortHolderScope() {

    var id: String? = null

    private val nodes = ArrayList<INode>()

    fun createHost() = Host(
        PortContainer(inputs, outputs, ioPuts),
        id ?: throw IllegalArgumentException("No id provided"),
        nodes
    )

    fun addNode(node: INode) = nodes.add(node)
}

open class PortHolderScope {

    internal val inputs = ArrayList<Port.Input<*>>()
    internal val outputs = ArrayList<Port.Output<*>>()
    internal val ioPuts = ArrayList<Port.IoPut<*>>()

    fun <T : Any> addInput(input: Port.Input<T>) = inputs.add(input)

    fun <T : Any> addOutput(output: Port.Output<T>) = outputs.add(output)

    fun <T : Any> addIoPut(ioPut: Port.IoPut<T>) = ioPuts.add(ioPut)
}

fun network(block: NetworkScope.() -> Unit): Network = NetworkScope().apply(block).createNetwork()

class NetworkScope {

    private val hosts = ArrayList<IHost>()

    fun addHost(host: IHost) = hosts.add(host)

    fun createNetwork() = Network(hosts)
}

class Network(val hosts: List<IHost>)