package other.pkg.filter

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogOutput
import dev.zieger.utils.log.LogScope
import dev.zieger.utils.log.callOrigin
import dev.zieger.utils.log.calls.logD
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.millis
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CallOriginTest : AnnotationSpec() {

    @BeforeEach
    fun beforeEach() {
        LogScope.Log.messageBuilder.logWithOriginMethodNameName = true
        LogScope.Log.output = LogOutput {
//            callOriginException?.printStackTrace()
            println(builtMessage)
        }
    }

    @Test
    fun testInsideLambda() {
        apply {
            1234.let { n ->
                println(".${Exception().callOrigin()}")
                n logD {
                    it.let { println("." + Exception().callOrigin()) }
                    "$it"
                }
            }
        }
    }

    @Test
    fun testIndirectCall() = runBlocking {
        val testLog2: suspend (suspend () -> Unit) -> Unit = { block: suspend () -> Unit ->
            block()
        }
        val testLog: suspend () -> Unit = suspend {
            delay(100.millis)
            testLog2 {
                Log.v("testing log")
            }
        }
        launch {
            testLog()
        }.join()
    }
}