package dev.zieger.utils.misc

import dev.zieger.utils.misc.parameterMix.mix
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.util.*

class MixTest : AnnotationSpec() {

    data class Parameters(val first: Int, val second: Double, val third: Long) {
        constructor(map: Map<String, Number>) :
                this(map["first"]!!.toInt(), map["second"]!!.toDouble(), map["third"]!!.toLong())
    }

    @Test
    fun testMix() = runBlocking {
        val maps = LinkedList<Parameters>()
        mix(builder = {
            Parameters::first with ((10 until 30 step 2) + (30 until 60 step 5) + (60 until 100 step 10))
            Parameters::second with listOf(5, 10)
            Parameters::third with 5L//Parameter.Random(Parameters::third, 2, Parameter.Random.Companion.TYPE.LONG, 5L, 10L)
        }, instanceFactory = {
            Parameters(it)
        }) {
            maps += this
            println(this)
        }.collect()

        maps.size shouldBe 42
    }.asUnit()
}