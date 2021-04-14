package dev.zieger.utils.coroutines

import io.kotest.core.spec.style.FunSpec

internal class ShellScopeTest : FunSpec({

    test("test shell scope") {
        shell {
            +"ls"
            +"ls /"
            +"ip a"
        }
    }
})