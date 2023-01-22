package other.pkg.filter

import dev.zieger.utils.log.LogScope
import dev.zieger.utils.log.callOrigin
import dev.zieger.utils.log.calls.logD
import io.kotest.core.spec.style.AnnotationSpec

class CallOriginTest : AnnotationSpec() {

    @Test
    fun testInsideLambda() {
        LogScope.Log.messageBuilder.logWithOriginMethodNameName = true

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
}