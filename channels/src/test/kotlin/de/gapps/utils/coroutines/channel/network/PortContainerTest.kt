package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.misc.asUnit
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class PortContainerTest : AnnotationSpec() {

    private lateinit var input0: Port.Input<Int>
    private lateinit var output0: Port.Output<Int>
    private lateinit var portContainer: IPortContainer

    @Before
    fun before() = runBlocking {
        input0 = Input("Input0")
        output0 = Output("Output0")
        portContainer = input0 + output0
        portContainer.onPortUpdated = {}
    }.asUnit()

    @Test
    fun testPortContainerTest() = runBlocking {

    }.asUnit()
}