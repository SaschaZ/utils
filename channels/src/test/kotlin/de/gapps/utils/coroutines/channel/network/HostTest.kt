package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.BeforeEach
import org.junit.Ignore
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class HostTest : AnnotationSpec() {

    private fun testNode(id: String) =
        Node(Input<Int>("${id}Input0") + Output<Int>("${id}Output0"), id) {
            Log.d("received value ${inputs.values.mapNotNull { it.poll() }.first().value}")
            @Suppress("UNCHECKED_CAST")
            node.outputForId<Int>("${id}Output0").send(ioPuts.mapNotNull { it.value.poll() }.first().value as INodeValue<Int>)
        }

    private lateinit var host: IHost

    @BeforeEach
    fun before() = runBlocking {
        host = Host(Input<Int>("Host0Input0") + Output<Int>("Host0Output0"), "Host0")
    }.asUnit()

    @Ignore
    @Test
    fun testWithTwoNodes() = runBlocking {
        host.addNode(testNode("Node0"))
        host.addNode(testNode("Node1"))

        host.linkPorts("Host0Input0" to "Node0Input0")
        host.linkPorts("Node0Output0" to "Node1Input0")
        host.linkPorts("Node1Output0" to "Host0Output0")

        host.sendScopeForId<Int>("Host0Input0") {
            send(NodeValue(1337))
            close()
        }
        withTimeout(100L) {
            host.receiveScopeForId<Int>("Host0Output0") {
                val value = receive()
                println("result=$value")
                assertEquals(1337, value.value)
            }
        }
    }.asUnit()

    @Test
    fun testAlone() = runBlocking {
        host.linkPorts("Host0Input0" to "Host0Output0")

        host.sendScopeForId<Int>("Host0Input0") {
            send(NodeValue(1337))
//            close()
        }
        withTimeout(100L) {
            host.receiveScopeForId<Int>("Host0Output0") {
                val value = receive()
                println("result=$value")
                assertEquals(1337, value.value)
            }
        }
    }.asUnit()
}