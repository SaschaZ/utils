package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.assertEquals

class LinkTest : AnnotationSpec() {

    @Before
    fun before() = runBlocking {

    }.asUnit()

    @After
    fun after() = runBlocking {

    }.asUnit()

    @Test
    fun testLink() = runBlocking {
        val in1 = Input<Int>("in1")
        val out1 = Output<Int>("out1")
        in1 link out1
        withTimeout(100L) {
            in1.internalChannel.send(NodeValue(1337))
            assertEquals(out1.internalChannel.receive().value, 1337)
        }
    }.asUnit()

    @Ignore
    @Test
    fun testLinkWithHost() = runBlocking {
        @Suppress("UNCHECKED_CAST")
        val node = Node(
            Input<Int>("Node0Input0") + Output<Int>("Node0Output0"),
            "Node0"
        ) {
            Log.v("received ${inputs.values.first()}")
            outputs["Node0Output0"]?.send(inputs.values.first() as INodeValue<Int>)
        }
        node.host = mockk()

        val host = Host(Input<Int>("Host0Input0") + Output<Int>("Host0Output0"), "Host0")
        host.addNode(node)

        host.linkPorts("Host0Input0" to "Node0Input0")
        host.linkPorts("Node0Output0" to "Host0Output0")

        host.sendScopeForId<Int>("Host0Input0") {
            send(NodeValue(1337))
        }
        withTimeout(100L) {
            host.receiveScopeForId<Int>("Host0Output0") {
                assertEquals(receive().value, 1337)
            }
        }
    }.asUnit()
}