package dev.zieger.utils.koin

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import org.koin.dsl.module

class DITest : AnnotationSpec() {

    @Test
    fun testDi() = runBlocking {
        var released = 0
        val di = DI {
            arrayOf(module {
                single(named("0")) { DiRelease { released++ } }
                single(named("1")) { DiRelease { released++ } }
            })
        }

        di.release()
        released shouldBe 2
    }
}
