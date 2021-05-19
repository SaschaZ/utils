@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "BooleanLiteralArgument")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.*
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.delegates.OnChangedTest.OnChangedResults.OnChangedTestResultInput
import dev.zieger.utils.delegates.OnChangedTest.OnChangedResults.OnChangedTestResultOutput
import dev.zieger.utils.misc.DataClass
import dev.zieger.utils.time.duration.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test
import kotlin.random.Random

class OnChangedTest {

    sealed class OnChangedResults {
        data class OnChangedTestResultInput(
            val inputValue: TestValueContainer,
            val veto: Boolean,
            val clearPrevious: Boolean
        ) : OnChangedResults()

        data class OnChangedTestResultOutput(
            val newValue: Int,
            val propertyValue: Int,
            val newVeto: Boolean,
            val previousCleared: Boolean,
            val onChangedScope: IOnChangedScope<TestValueContainer>
        ) : OnChangedResults()
    }

    data class OnChangedParams(val map: Map<String, ParamInstance<*>>) : DataClass() {

        val newValueFactory: (Int?) -> Int by bind(map)

        val storePreviousValues: Boolean by bind(map)
        val notifyForInitial: Boolean by bind(map)
        val notifyOnChangedOnly: Boolean by bind(map)

        val scope: CoroutineScope? by bind(map)
        val mutex: Mutex by bind(map)
        val veto: (Int) -> Boolean by bind(map)
        val doClearPrevValues: (Int) -> Boolean by bind(map)
        val suspendedListener: Boolean by bind(map)
    }

    data class TestValueContainer(
        val value: Int = -1,
        val idx: Int = -1
    ) {
        override fun equals(other: Any?): Boolean = value == (other as? TestValueContainer)?.value
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "[$value - $idx]"
    }

    private fun parameter(block: OnChangedParams.() -> Channel<OnChangedResults>) = parameterMixCollect(
        { OnChangedParams(it) },
        param("newValueFactory", { old: Int? -> Random.nextInt(0..9) }),
        param("storePreviousValues", true, false),
        param("notifyForInitial", true, false),
        param("notifyOnChangedOnly", true, false),
        param("scope", TestCoroutineScope(), null),
        param("mutex", Mutex()),
        param("veto", { value: Int -> Random.nextBoolean(0.1f) }),
        param("doClearPrevValues", { value: Int -> Random.nextBoolean(0.1f) }),
        param("suspendedListener", /*true, */false)
    ) { block() }

    @Test
    fun testOnChanged() = runTest(1.minutes) {
        parameter {
            var newValue: Int = -1
            var newVeto = false
            var newClearCache = false
            val results = Channel<OnChangedResults>(Channel.UNLIMITED)

            var propertyValue: () -> TestValueContainer = { TestValueContainer() }
            var testProperty by OnChanged(
                OnChangedParams(
                    TestValueContainer(), storeRecentValues = storePreviousValues,
                    notifyForInitial = notifyForInitial,
                    notifyOnChangedValueOnly = notifyOnChangedOnly,
                    scope = scope, mutex = mutex, veto = { newVeto },
                    onChangedS = { v ->
                        onChangedListenerBlock(newClearCache, results, newValue, propertyValue, newVeto)
                    },
                    onChanged = { v ->
                        onChangedListenerBlock(newClearCache, results, newValue, propertyValue, newVeto)
                    })
            )
            propertyValue = { testProperty }

            repeat(1000) { i ->
                newValueFactory(newValue).apply {
                    newValue = this
                    newVeto = veto(this)
                    newClearCache = doClearPrevValues(this)
                }
                testProperty = TestValueContainer(newValue, i).apply {
                    results.offer(OnChangedTestResultInput(this, newVeto, newClearCache))
                }
            }

            results
        }.verify()
    }

    @Test
    fun testOnChangedWithParent() = runTest(1.minutes) {
        parameter {
            var newValue: Int = -1
            var newVeto = false
            var newClearCache = false
            val results = Channel<OnChangedResults>(Channel.UNLIMITED)

            var propertyValue: () -> TestValueContainer = { TestValueContainer() }
            var testProperty by OnChangedWithParent(
                OnChangedParamsWithParent(
                    TestValueContainer(), storeRecentValues = storePreviousValues,
                    notifyForInitial = notifyForInitial,
                    notifyOnChangedValueOnly = notifyOnChangedOnly,
                    scope = scope, mutex = mutex, veto = { newVeto },
                    onChangedS = null/*{ v ->
                        if (suspendedListener && scope != null)
                            onChangedListenerBlock(newClearCache, results, newValue, propertyValue, newVeto)
                    }*/,
                    onChanged = { v ->
                        if (!suspendedListener || scope == null)
                            onChangedListenerBlock(newClearCache, results, newValue, propertyValue, newVeto)
                    })
            )
            propertyValue = { testProperty }

            repeat(1000) { i ->
                newValueFactory(newValue).apply {
                    newValue = this
                    newVeto = veto(this)
                    newClearCache = doClearPrevValues(this)
                }
                testProperty = TestValueContainer(newValue, i).apply {
                    results.offer(OnChangedTestResultInput(this, newVeto, newClearCache))
                }
            }

            results
        }.verify()
    }

    private fun IOnChangedScopeWithParent<Any?, @UnsafeVariance TestValueContainer>.onChangedListenerBlock(
        newClearCache: Boolean,
        results: Channel<OnChangedResults>,
        newValue: Int,
        propertyValue: () -> TestValueContainer,
        newVeto: Boolean
    ) {
        if (newClearCache) clearPreviousValues()
        results.offer(
            OnChangedTestResultOutput(
                newValue, propertyValue().value, newVeto, newClearCache, this
            )
        )
    }

    private suspend fun Map<OnChangedParams, Channel<OnChangedResults>>.verify() {
        delay(1000)

        for ((input, result) in this) {
//            println("\n\n$input")

            var prevInIdx = -1
            var prevOutIdx = if (input.notifyForInitial) -2 else -1
            var directInput = OnChangedTestResultInput(TestValueContainer(), false, false)
            val vetoUpComing = ArrayList<Int>()
            val sameValueUpComing = ArrayList<Int>()
            var isFirstNotification = true

            result.close()
            result.toList().sortedBy {
                (it as? OnChangedTestResultInput)?.inputValue?.idx
                    ?: (it as OnChangedTestResultOutput).onChangedScope.value.idx
            }.forEach { r ->
                when (r) {
                    is OnChangedTestResultInput -> {
//                        println(r)

                        val idx = r.inputValue.idx
                        (idx - prevInIdx) assert 1
                        if (r.veto) vetoUpComing.add(idx)
                        else if (r.inputValue.value == directInput.inputValue.value && input.notifyOnChangedOnly)
                            sameValueUpComing.add(idx)
                        else directInput = r

                        prevInIdx = idx
                    }
                    is OnChangedTestResultOutput -> {
//                        println("\t$r")

                        val (value, idx) = r.onChangedScope.value

                        (idx - prevOutIdx) assert 1 + sameValueUpComing.size + vetoUpComing.size
                        sameValueUpComing.clear()
                        vetoUpComing.clear()

                        r.onChangedScope.isInitialNotification assert (value == -1 && idx == -1
                                && r.onChangedScope.previousValues.isNullOrEmpty()
                                && isFirstNotification
                                && input.notifyForInitial)

                        r.newValue assert r.onChangedScope.value.value
                        r.propertyValue assert r.onChangedScope.value.value

                        (r.previousCleared || r.onChangedScope.isInitialNotification || !input.storePreviousValues) assert
                                r.onChangedScope.previousValues.isNullOrEmpty()

                        prevOutIdx = idx
                        isFirstNotification = false
                    }
                }
            }
        }
    }
}


fun Random.Default.nextInt(range: IntRange) = nextInt(range.first, range.last)
fun Random.Default.nextBoolean(percent: Float) = nextFloat() <= percent