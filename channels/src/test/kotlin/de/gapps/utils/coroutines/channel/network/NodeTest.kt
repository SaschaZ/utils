package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import jdk.nashorn.internal.ir.annotations.Ignore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Ignore
class NodeTest : AnnotationSpec() {

    private lateinit var node: Node
    private val updatedInputValues = ArrayList<INodeValue<*>>()
    private val updatedIoPutValues = ArrayList<INodeValue<*>>()

    @Before
    fun before() = runBlocking {
        updatedInputValues.clear()
        updatedIoPutValues.clear()

        node = Node(
            Input<Int>("Node0Input0") + Output<Int>("Node0Output0"),
            "Node0"
        ) {
            updatedInputValues.addAll(inputs.values.mapNotNull { it.poll() })
            updatedIoPutValues.addAll(ioPuts.values.mapNotNull { it.poll() })
            @Suppress("UNCHECKED_CAST")
            node.outputForId<Int>("Node0Output0").send(inputs.values.first() as INodeValue<Int>)
        }
        node.host = mockk()
    }.asUnit()

    @Test
    fun testPortForId() = runBlocking {
        assertNotNull(node.inputForId<Int>("Node0Input0"))
        assertNotNull(node.outputForId<Int>("Node0Output0"))
    }.asUnit()

    @Ignore
    @Test
    fun testInput() = runBlocking {
        node.inputForId<Int>("Node0Input0").internalChannel.send(NodeValue(1337))
        delay(1.seconds)
        assertEquals(1337, updatedInputValues.firstOrNull()?.value)
    }.asUnit()

    @Ignore
    @Test
    fun testOutput() = runBlocking {
        node.inputForId<Int>("Node0Input0").internalChannel.send(NodeValue(1337))
        withTimeout(100L) {
            assertEquals(1337, node.outputForId<Int>("Node0Output0").internalChannel.receive().value)
        }
    }
}