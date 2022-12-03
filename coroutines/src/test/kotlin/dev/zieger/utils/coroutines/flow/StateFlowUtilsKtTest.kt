package dev.zieger.utils.coroutines.flow

import dev.zieger.utils.coroutines.scope.ScopeHolder
import dev.zieger.utils.coroutines.trigger.Value
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.*

import kotlin.math.pow

internal class StateFlowUtilsKtTest : AnnotationSpec(), ScopeHolder {

    override val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    @Test
    suspend fun testStateFlow() {
        val v0 = Value(scope, 0, printDebug = true, debugName = "v0")
        val v1 = Value(scope, 0, v0, printDebug = true, debugName = "v1") {
            value(v0.value)
        }
        val v2 = Value(scope, 0.0, v1, printDebug = true, debugName = "v2") {
            value(v1.value.toDouble().pow(2.0))
        }
        val v3 = Value(scope, 0.0, v0, v1, v2, printDebug = true, debugName = "v3") {
            value(v0.value + v1.value + v2.value)
        }

        repeat(10) { v0.value(it + 1) }
    }
}

