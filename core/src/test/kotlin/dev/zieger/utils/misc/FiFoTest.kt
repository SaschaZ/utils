package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.assertion2.hasSameContent
import dev.zieger.utils.core_testing.assertion2.isTrue
import io.kotest.core.spec.style.FunSpec

internal class FiFoTest : FunSpec({

    lateinit var fifo: FiFo<Int>

    beforeEach {
        fifo = FiFo(3)
    }

    test("fifo") {
        fifo.isEmpty().isTrue()

        repeat(5) { fifo.put(it) }
        fifo hasSameContent listOf(2, 3, 4)
    }
})