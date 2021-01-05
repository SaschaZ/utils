package dev.zieger.utils

import dev.zieger.utils.log2.Log
import io.kotest.core.spec.style.FunSpec

internal class OsInfoTest : FunSpec({
    test("testOsType") {
        Log.v(OsInfo.type)
    }
})