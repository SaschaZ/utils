package dev.zieger.utils.misc

import io.kotest.core.spec.style.FunSpec

class HexTest : FunSpec({

    test("hex") {
        println(0x00000000L.hex4)
    }
})