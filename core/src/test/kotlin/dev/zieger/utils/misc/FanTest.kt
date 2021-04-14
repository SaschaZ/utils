package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.assertion2.hasSameContent
import dev.zieger.utils.coroutines.flow.fan
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList

class FanTest : FunSpec({

    test("fan list") {
        val input = (1..100).toList()
        val block: suspend (Int) -> Int = { it * it }
        input.fan { block(it) } hasSameContent input.map { block(it) }
    }

    test("fan flow"){
        val rawInput = (1..300)
        val input = rawInput.asFlow()
        val block: suspend (Int) -> Int = { it * it }
        input.fan { block(it) }.toList() hasSameContent rawInput.map { block(it) }
    }
})