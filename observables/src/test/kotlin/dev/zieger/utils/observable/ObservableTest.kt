package dev.zieger.utils.observable

import dev.zieger.utils.observables.SimpleObservable
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class ObservableTest : AnnotationSpec() {

    private lateinit var context: CoroutineContext

    @BeforeEach
    fun beforeEach() {
        context = newSingleThreadContext("")
    }

    @Test
    fun testSimple() = runBlocking(context) {
        val obs0 = SimpleObservable(0)
        var val0 by obs0

        obs0.observe {  }

        val0 = 1
    }
}