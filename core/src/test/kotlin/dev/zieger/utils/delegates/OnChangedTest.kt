@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "BooleanLiteralArgument")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.TestCoroutineScope
import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.mix.ParamInstance
import dev.zieger.utils.core_testing.mix.bind
import dev.zieger.utils.core_testing.mix.param
import dev.zieger.utils.core_testing.mix.parameterMixCollect
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.withLock
import dev.zieger.utils.misc.runEachIndexed
import dev.zieger.utils.time.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test
import kotlin.random.Random

class OnChangedTest {

    data class OnChangedResult(
        val params: OnChangedParams,
        val rawInput: Int,
        val newValue: TestValueContainer,
        val propertyValue: TestValueContainer,
        val veto: Boolean,
        val previousCleared: Boolean,
        val onChangedScope: IOnChangedScope<TestValueContainer>
    )

    data class OnChangedParams(val params: Map<String, ParamInstance<*>>) {

        val initial: Int by bind(params)
        val inputs: List<Int> by bind(params)

        val storePreviousValues: Boolean by bind(params)
        val notifyForInitial: Boolean by bind(params)
        val notifyOnChangedOnly: Boolean by bind(params)

        val scope: CoroutineScope by bind(params)
        val mutex: Mutex by bind(params)
        val safeSet: Boolean by bind(params)

        val map: List<Int?> by bind(params)
        val veto: List<Boolean> by bind(params)

        val doClearPrevValues: List<Boolean> by bind(params)
        val suspendedListener: Boolean by bind(params)
    }

    data class TestValueContainer(
        val value: Int = -1,
        val idx: Int = -1
    ) {
        override fun equals(other: Any?): Boolean = value == (other as? TestValueContainer)?.value
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "[$value - $idx]"
    }

    private fun parameter(block: OnChangedParams.() -> Channel<OnChangedResult>) =
        parameterMixCollect(
            { OnChangedParams(it) },
            param("initial", 0),
            param<List<Int>>("inputs", (0..9).map { Random.nextInt(0..9) }),
            param("storePreviousValues", true, false),
            param("notifyForInitial", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", TestCoroutineScope()),
            param("mutex", Mutex()),
            param("safeSet", true, false),
            param<List<Int?>>("map", (0..9).map { if (Random.nextBoolean(0.1f)) Random.nextInt() else null }),
            param<List<Boolean>>("veto", (0..9).map { Random.nextBoolean(0.1f) }),
            param<List<Boolean>>("doClearPrevValues", (0..9).map { Random.nextBoolean(0.1f) }),
            param("suspendedListener", true, false)
        ) { block() }

    @Test
    fun testOnChanged() = runTest(1.minutes) {
        parameter {
            var newValue = 0
            var newVeto = false
            var newClearCache = false
            val results = Channel<OnChangedResult>(Channel.UNLIMITED)

            var propertyValue: () -> TestValueContainer = { TestValueContainer() }
            var testProperty by OnChanged(
                OnChangedParams(
                    TestValueContainer(), storeRecentValues = storePreviousValues,
                    notifyForInitial = notifyForInitial,
                    notifyOnChangedValueOnly = notifyOnChangedOnly,
                    scope = scope, mutex = mutex, veto = { newVeto },
                    onChangedS = { v ->
                        if (suspendedListener)
                            onChangedListenerBlock(
                                this@parameter,
                                newValue,
                                v,
                                results,
                                propertyValue(),
                                mutex,
                                newVeto,
                                newClearCache
                            )
                    },
                    onChanged = { v ->
                        if (!suspendedListener) scope.launch {
                            onChangedListenerBlock(
                                this@parameter,
                                newValue,
                                v,
                                results,
                                propertyValue(),
                                mutex,
                                newVeto,
                                newClearCache
                            )
                        }
                    })
            )
            propertyValue = { testProperty }

            inputs.runEachIndexed { idx ->
                newValue = this
                newVeto = veto[idx % veto.size]
                newClearCache = doClearPrevValues[idx % veto.size]
                testProperty = TestValueContainer(this, idx)
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
            val results = Channel<OnChangedResult>(Channel.UNLIMITED)

            var propertyValue: () -> TestValueContainer = { TestValueContainer() }
            var testProperty by OnChangedWithParent(
                OnChangedParamsWithParent(
                    TestValueContainer(), storeRecentValues = storePreviousValues,
                    notifyForInitial = notifyForInitial,
                    notifyOnChangedValueOnly = notifyOnChangedOnly,
                    scope = scope, mutex = mutex, veto = { newVeto },
                    onChangedS = { v ->
                        if (suspendedListener)
                            onChangedListenerBlock(
                                this@parameter,
                                newValue,
                                v,
                                results,
                                propertyValue(),
                                mutex,
                                newVeto,
                                newClearCache
                            )
                    },
                    onChanged = { v ->
                        if (!suspendedListener) scope.launch {
                            onChangedListenerBlock(
                                this@parameter,
                                newValue,
                                v,
                                results,
                                propertyValue(),
                                mutex,
                                newVeto,
                                newClearCache
                            )
                        }
                    })
            )
            propertyValue = { testProperty }

            inputs.runEachIndexed { idx ->
                newValue = this
                newVeto = veto[idx % veto.size]
                newClearCache = doClearPrevValues[idx % veto.size]
                testProperty = TestValueContainer(this, idx)
            }

            results
        }.verify()
    }

    private suspend fun <P : Any?, S : IOnChangedScopeWithParent<P, TestValueContainer>> S.onChangedListenerBlock(
        params: OnChangedParams,
        rawInputValue: Int,
        newValue: TestValueContainer,
        results: Channel<OnChangedResult>,
        propertyValue: TestValueContainer,
        mutex: Mutex,
        newVeto: Boolean,
        newClearCache: Boolean
    ) {
        if (newClearCache) clearPreviousValues()
        results.offer(
            OnChangedResult(
                params, rawInputValue, newValue, propertyValue, newVeto, newClearCache, OnChangedScope(
                    value,
                    thisRef,
                    previousValue,
                    mutex.withLock { ArrayList(previousValues) },
                    clearPreviousValues,
                    isInitialNotification
                )
            )
        )
    }

    private suspend fun Map<OnChangedParams, Channel<OnChangedResult>>.verify() {
        delay(1000)

        toList().forEachIndexed { inputIdx, (input, result) ->
//            println("\n\n$input")

            var prevInIdx = -1
            var prevOutIdx = if (input.notifyForInitial) -2 else -1
            var isFirstNotification = true

            result.close()
            result.toList().sortedBy { it.newValue.idx }.forEach { r ->
                println(r)

                val idx = r.newValue.idx
                (idx - prevInIdx) isEqual 1
                prevInIdx = idx


//                is OnChangedTestResultOutput -> {
////                        println("\t$r")
//
//                val (value, idx) = r.onChangedScope.value
//
//                (idx - prevOutIdx) assert 1 + sameValueUpComing.size + vetoUpComing.size
//                sameValueUpComing.clear()
//                vetoUpComing.clear()
//
//                r.onChangedScope.isInitialNotification assert (value == -1 && idx == -1
//                        && r.onChangedScope.previousValues.isNullOrEmpty()
//                        && isFirstNotification
//                        && input.notifyForInitial)
//
//                r.newValue assert r.onChangedScope.value.value
//                r.propertyValue assert r.onChangedScope.value.value
//
//                (r.previousCleared || r.onChangedScope.isInitialNotification || !input.storePreviousValues) assert
//                        r.onChangedScope.previousValues.isNullOrEmpty()
//
//                prevOutIdx = idx
//                isFirstNotification = false
//            }
            }
        }
    }
}


fun Random.Default.nextInt(range: IntRange) = nextInt(range.first, range.last)
fun Random.Default.nextBoolean(percent: Float) = nextFloat() <= percent