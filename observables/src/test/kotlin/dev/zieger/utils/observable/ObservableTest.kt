@file:Suppress("unused")

package dev.zieger.utils.observable

import dev.zieger.utils.observables.MutableObservable
import dev.zieger.utils.observables.Number
import dev.zieger.utils.observables.getAndIncrement
import dev.zieger.utils.observables.incrementAndGet
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCaseConfig
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class ObservableTest : AnnotationSpec() {

    private lateinit var context: CoroutineContext

    init {
        defaultTestConfig = TestCaseConfig(invocations = 100000, threads = 16)
    }

    @BeforeAll
    fun beforeAll() {
        context = newSingleThreadContext("")
    }

    @Test
    fun testObservableSameThread() = runBlocking(context) {
        var first: Number? = null
        val obs0 = MutableObservable(Number(0), primaryContext = context) {
            first = it
        }
        val val0 by obs0

        var second: Number? = null
        val unObserve = obs0.observe {
            second = it
        }

        obs0.incrementAndGet() shouldBe 1
        val0 shouldBe 1
        obs0.suspendUntil(Number(1)) shouldBe 1
        first shouldBe 1
        second shouldBe 1

        obs0.getAndIncrement()
        val0 shouldBe 2
        obs0.suspendUntil(Number(2)) shouldBe 2
        first shouldBe 2
        second shouldBe 2

        unObserve()

        obs0.incrementAndGet()
        val0 shouldBe 3
        obs0.suspendUntil(Number(3)) shouldBe 3
        first shouldBe 3
        second shouldBe 2
    }

    @Test
    fun testObservableTwoThreads() = runBlocking {
        var first: Int? = null
        val obs0 = MutableObservable(0) {
            first = it
            if (it == 3)
                offerValue(4)
        }
        val val0 by obs0

        var second: Int? = null
        val unObserve = obs0.observe { second = it }

        obs0.changeValue { 1 }
        val0 shouldBe 1
        obs0.suspendUntil(1) shouldBe 1
        first shouldBe 1
        second shouldBe 1

        obs0.setValue(2)
        val0 shouldBe 2
        obs0.suspendUntil(2) shouldBe 2
        first shouldBe 2
        second shouldBe 2

        unObserve()

        obs0.offerValue(3)
        obs0.suspendUntil(4) shouldBe 4
        first shouldBe 4
        second shouldBe 2
    }

    @Test
    fun testImmutable() = runBlocking {
        val _obs = MutableObservable(false)
        val obs = _obs.toImmutableObservable()
        var result: Boolean? = null
        obs.observe(notifyForInitial = true) { result = it }
        result shouldBe false
        obs.value shouldBe false
        _obs.value shouldBe false

        _obs.setValue(true)
        result shouldBe true
        obs.value shouldBe true
        _obs.value shouldBe true
    }
}
