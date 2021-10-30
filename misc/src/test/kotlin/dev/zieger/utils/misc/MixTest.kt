package dev.zieger.utils.misc

import dev.zieger.utils.misc.parameterMix.mix
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.util.*

class MixTest : AnnotationSpec() {

    class Parameters(val map: Map<String, Any?>) {
        val first: Int by map
        val second: Int by map
        val third: Long by map

        override fun toString() = "Parameters($first, $second, $third)"
    }

    @Test
    fun testMix() = runBlocking {
        val maps = LinkedList<Parameters>()
        mix(builder = {
            Parameters::first with ((10..30 step 2) + (30..60 step 5) + (60..100 step 10))
            Parameters::second with listOf(4, 10)
            Parameters::third with 5L//Parameter.Random(Parameters::third, 2, Parameter.Random.Companion.TYPE.LONG, 5L, 10L)
        }, instanceFactory = {
            Parameters(it)
        }) {
            maps += this
            println(this)
        }.collect()

        maps.size shouldBe 46
    }.asUnit()
}