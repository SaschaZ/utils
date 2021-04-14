@file:Suppress("unused")

package dev.zieger.utils.log

import io.kotest.core.spec.style.FunSpec


internal class LogTest : FunSpec({

    test("spam filter") {
        Log.w("before test")
    }
})