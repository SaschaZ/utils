package dev.zieger.utils.coroutines.channel.network

import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PortContainerTest {

    private lateinit var input0: Port.Input<Int>
    private lateinit var output0: Port.Output<Int>
    private lateinit var portContainer: IPortContainer

    @BeforeEach
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