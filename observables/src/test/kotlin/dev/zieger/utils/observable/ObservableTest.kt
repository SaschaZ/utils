@file:Suppress("unused")

package dev.zieger.utils.observable

import dev.zieger.utils.observables.MutableObservable
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCaseConfig
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext

class ObservableTest : AnnotationSpec() {

    private lateinit var context: CoroutineContext

    init {
        defaultTestConfig = TestCaseConfig(invocations = 10000)
    }

    @BeforeAll
    fun beforeAll() {
        context = newSingleThreadContext("")
    }

    @Test
    fun testObservableSameThread() = runBlocking(context) {
        var first: Int? = null
        val obs0 = MutableObservable(0) {
            first = it
        }
        var val0 by obs0

        var second: Int? = null
        val unObserve = obs0.observe {
            second = it
        }

        val0 = 1
        val0 shouldBe 1
        obs0.suspendUntil(1) shouldBe 1
        first shouldBe 1
        second shouldBe 1

        val0 = 2
        val0 shouldBe 2
        obs0.suspendUntil(2) shouldBe 2
        first shouldBe 2
        second shouldBe 2

        unObserve()

        val0 = 3
        val0 shouldBe 3
        obs0.suspendUntil(3) shouldBe 3
        first shouldBe 3
        second shouldBe 2
    }

    @Test
    fun testObservableTwoThreads() = runBlocking {
        var first: Int? = null
        val obs0 = MutableObservable(0, context) {
            first = it
        }
        val val0 by obs0

        var second: Int? = null
        val unObserve = obs0.observe { second = it }

        obs0.changeValue { 1 }
        val0 shouldBe 1
        verify("suspendUntil(1)") { obs0.suspendUntil(1) shouldBe 1 }
        first shouldBe 1
        second shouldBe 1

        obs0.changeValue { 2 }
        val0 shouldBe 2
        verify("suspendUntil(2)") { obs0.suspendUntil(2) shouldBe 2 }
        first shouldBe 2
        second shouldBe 2

        unObserve()

        obs0.changeValue { 3 }
        val0 shouldBe 3
        verify("suspendUntil(3)") { obs0.suspendUntil(3) shouldBe 3 }
        first shouldBe 3
        second shouldBe 2
    }

    @Test
    fun testSimpleObservable() = runBlocking {
        var first: Int? = null
        val obs0 = MutableObservable(0)
        obs0.observe {
            first = it
//            if (it == 3)
//                current = 4
        }
        var val0 by obs0

        var second: Int? = null
        val unObserve = obs0.observe { second = it }

        val0 = 1
        val0 shouldBe 1
        first shouldBe 1
        second shouldBe 1

        val0 = 2
        val0 shouldBe 2
        first shouldBe 2
        second shouldBe 2

        unObserve()

        val0 = 3
        val0 shouldBe 3
        first shouldBe 3
        second shouldBe 2
    }
}

suspend inline fun verify(title: String, timeout: Long = 5_000, crossinline block: suspend () -> Unit) {
    try {
        withTimeout(timeout) { block() }
    } catch (t: Throwable) {
        throw VerifyException(title, t)
    }
}

open class VerifyException(title: String, cause: Throwable) :
    IllegalStateException("Verify for '$title' failed because of '$cause'")