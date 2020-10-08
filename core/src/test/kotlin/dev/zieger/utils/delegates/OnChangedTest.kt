@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "BooleanLiteralArgument")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.FlakyTest
import dev.zieger.utils.core_testing.TestCoroutineScope
import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.rem
import dev.zieger.utils.core_testing.mix.ParamInstance
import dev.zieger.utils.core_testing.mix.bind
import dev.zieger.utils.core_testing.mix.param
import dev.zieger.utils.core_testing.mix.parameterMixCollect
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.coroutines.withLock
import dev.zieger.utils.misc.joinToStringIndexed
import dev.zieger.utils.misc.name
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.runEachIndexed
import dev.zieger.utils.time.minutes
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test
import java.lang.Integer.max
import kotlin.random.Random

class OnChangedTest : FlakyTest() {

    data class OnChangedResult(
        val isSuspended: Boolean,
        val params: OnChangedTestParams,
        val rawInput: Int,
        val newValue: TestValueContainer,
        val propertyValue: TestValueContainer,
        val veto: Boolean,
        val previousCleared: Boolean,
        val onChangedScope: IOnChangedScope<TestValueContainer>
    ) {
        override fun toString(): String = "OnChangedResult(\nisSuspended: $isSuspended\nparams: $params\n" +
                "rawInput: $rawInput\nnewValue: $newValue\npropertyValue: $propertyValue\nveto: $veto\n" +
                "previousCleared: $previousCleared\nonChangedScope: $onChangedScope)"
    }

    data class OnChangedTestParams(val params: Map<String, ParamInstance<*>>) {

        val initial: Int by bind(params)
        val inputs: List<Int> by bind(params)

        val storePreviousValues: Boolean by bind(params)
        val notifyForInitial: Boolean by bind(params)
        val notifyOnChangedOnly: Boolean by bind(params)

        val coroutineScope: CoroutineScope by bind(params)
        val mutex: Mutex by bind(params)
        val safeSet: Boolean by bind(params)

        val map: List<Int?> by bind(params)
        val veto: List<Boolean> by bind(params)

        val doClearPrevValues: List<Boolean> by bind(params)
        val suspendedListener: Boolean by bind(params)

        override fun toString(): String = "${this::class.name}(\n" +
                "\tinitial: $initial\n" +
                "\tinputs: ${inputs.joinToStringIndexed { idx, value -> "$idx: $value" }}\n" +
                "\tstorePreviousValues: $storePreviousValues\n" +
                "\tnotifyForInitial: $notifyForInitial\n" +
                "\tnotifyOnChangedOnly: $notifyOnChangedOnly\n" +
                "\tcoroutineScope: ${coroutineScope::class.name}\n" +
                "\tmutex: $mutex\n" +
                "\tsafeSet: $safeSet\n" +
                "\tsuspendedListener: $suspendedListener\n" +
                "\tmap: ${map.joinToStringIndexed { idx, value -> "$idx: $value" }}\n" +
                "\tveto: ${veto.joinToStringIndexed { idx, value -> "$idx: $value" }}\n" +
                "\tdoClearPrevious: ${doClearPrevValues.joinToStringIndexed { idx, value -> "$idx: $value" }})"
    }

    data class TestValueContainer(
        val value: Int = -1,
        val idx: Int = -1
    ) {
        override fun equals(other: Any?): Boolean = value == (other as? TestValueContainer)?.value
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "[$value - $idx]"
    }

    companion object {
        const val INPUT_VALUE_NUM = 100
    }

    private fun parameter(block: OnChangedTestParams.() -> Channel<OnChangedResult>) =
        parameterMixCollect(
            { OnChangedTestParams(it) },
            param("initial", 0),
            param<List<Int>>("inputs", (0..INPUT_VALUE_NUM).map { Random.nextInt(0..9) }),
            param("storePreviousValues", true, false),
            param("notifyForInitial", true, false),
            param("notifyOnChangedOnly", true, false),
            param("coroutineScope", TestCoroutineScope()),
            param("mutex", Mutex()),
            param("safeSet", true, false),
            param<List<Int?>>(
                "map",
                (0..INPUT_VALUE_NUM).map { Random.nextInt(0..9).nullWhen { Random.nextBoolean(0.9f) } }),
            param<List<Boolean>>("veto", (0..INPUT_VALUE_NUM).map { Random.nextBoolean(0.1f) }),
            param<List<Boolean>>("doClearPrevValues", (0..INPUT_VALUE_NUM).map { Random.nextBoolean(0.1f) }),
            param("suspendedListener", true, false)
        ) { block() }

    @Test
    fun testOnChanged() = runTest(5.minutes) {
        parameter {
            var newValue = 0
            var newVeto = false
            var newMap: Int? = null
            var newClearCache = false
            val results = Channel<OnChangedResult>(Channel.UNLIMITED)

            var propertyValue: () -> TestValueContainer = { TestValueContainer() }
            var testProperty by OnChanged(
                OnChangedParams(
                    initial = TestValueContainer(),
                    storeRecentValues = storePreviousValues,
                    notifyForInitial = notifyForInitial,
                    notifyOnChangedValueOnly = notifyOnChangedOnly,
                    scope = coroutineScope,
                    mutex = mutex,
                    map = { newMap?.let { nm -> TestValueContainer(nm, it.idx) } ?: it },
                    veto = { newVeto },
                    onChangedS = { v ->
                        if (suspendedListener)
                            onChangedListenerBlock(
                                true, this@parameter, newValue, v, results,
                                propertyValue(), newVeto, newClearCache
                            )
                    },
                    onChanged = { v ->
                        if (!suspendedListener) onChangedListenerBlock(
                            false, this@parameter, newValue, v, results,
                            propertyValue(), newVeto, newClearCache
                        )
                    })
            )
            propertyValue = { testProperty }

            inputs.runEachIndexed { idx ->
                newValue = this
                newVeto = veto[idx % veto.size]
                newMap = map[idx % map.size]
                newClearCache = doClearPrevValues[idx % veto.size]
                testProperty = TestValueContainer(newValue, idx)
            }

            results
        }.verify()
    }

    @Test
    fun testOnChangedWithParent() = runTest(5.minutes) {
        parameter {
            var newValue: Int = -1
            var newVeto = false
            var newMap: Int? = null
            var newClearCache = false
            val results = Channel<OnChangedResult>(Channel.UNLIMITED)

            var propertyValue: () -> TestValueContainer = { TestValueContainer() }
            var testProperty by OnChangedWithParent(
                OnChangedParamsWithParent(
                    TestValueContainer(), storeRecentValues = storePreviousValues,
                    notifyForInitial = notifyForInitial,
                    notifyOnChangedValueOnly = notifyOnChangedOnly,
                    scope = coroutineScope,
                    mutex = mutex,
                    veto = { newVeto },
                    map = { newMap?.let { nm -> TestValueContainer(nm, it.idx) } ?: it },
                    onChangedS = { v ->
                        if (suspendedListener)
                            onChangedListenerBlock(
                                true, this@parameter, newValue, v, results,
                                propertyValue(), newVeto, newClearCache
                            )
                    },
                    onChanged = { v ->
                        if (!suspendedListener) coroutineScope.launch {
                            onChangedListenerBlock(
                                false, this@parameter, newValue, v, results,
                                propertyValue(), newVeto, newClearCache
                            )
                        }
                    })
            )
            propertyValue = { testProperty }

            inputs.runEachIndexed { idx ->
                newValue = this
                newVeto = veto[idx % veto.size]
                newMap = map[idx % map.size]
                newClearCache = doClearPrevValues[idx % veto.size]
                testProperty = TestValueContainer(this, idx)
            }

            results
        }.verify()
    }

    private fun <P : Any?, S : IOnChangedScopeWithParent<P, TestValueContainer>> S.onChangedListenerBlock(
        isSuspended: Boolean,
        params: OnChangedTestParams,
        rawInputValue: Int,
        newValue: TestValueContainer,
        results: Channel<OnChangedResult>,
        propertyValue: TestValueContainer,
        newVeto: Boolean,
        newClearCache: Boolean
    ) {
        if (newClearCache) clearPreviousValues()
        results.offer(
            OnChangedResult(
                isSuspended, params, rawInputValue, newValue, propertyValue, newVeto, newClearCache, copy()
            )
        )
    }

    private fun IOnChangedScopeWithParent<Any?, TestValueContainer>.copy(): IOnChangedScopeWithParent<Any?, TestValueContainer> {
        return OnChangedScopeWithParent(
            value, parent, propertyName, previousValue, ArrayList(previousValues),
            clearPreviousValues, isInitialNotification, valueChangedListener
        )
    }

    private suspend fun Map<OnChangedTestParams, Channel<OnChangedResult>>.verify() {
        delay(1000)

        toList().forEachIndexed { inputIdx, (input, result) ->
            var prevInIdx = -1

            result.close()
            result.toList().sortedBy { it.newValue.idx }.forEachIndexed { i, r ->
//                println("\n\nloopIdx: $inputIdx\nresult: $r\n")

                val idx = r.newValue.idx
                idx isEqual (prevInIdx + getIdxDiff(r, input, idx, prevInIdx)) % "IDX"

                r.newValue.value isEqual (if (r.onChangedScope.isInitialNotification) -1 else input.map[idx]
                    ?: input.inputs[idx]) % "VALUE"
                r.onChangedScope.previousValue?.value isEqual when {
                    r.onChangedScope.isInitialNotification -> null
                    idx == 0 -> -1
                    else -> (idx - getIdxDiff(r, input, idx, prevInIdx)).let {
                        input.map.getOrNull(it) ?: input.inputs.getOrNull(it) ?: -1
                    }
                } % "PREV_VALUE"

                prevInIdx = idx
            }
        }
    }

    private fun getIdxDiff(
        r: OnChangedResult,
        input: OnChangedTestParams,
        idx: Int,
        prevInIdx: Int
    ): Int = if (r.onChangedScope.isInitialNotification) 0 else 1 +
            input.run {
                var prev = -1
                (max(0, prevInIdx)..max(0, idx - 1)).count {
                    val value = map[it] ?: inputs[it]
                    (veto[it] || notifyOnChangedOnly && value == prev).also {
                        prev = value
                    }
                }
            }

    @Test
    fun smallTest() = runTest {
        var active by OnChanged(1) {
            println("`$propertyName` changed from $previousValue to $value")
            value = 100
        }
        active isEqual 1
        active = 2
        active isEqual 100
        active = 3
        active isEqual 100
    }

    @Test
    fun testChangeValue() = runTest(maxExecutions = 1) {
        val scope = IoCoroutineScope()
        val testObs = OnChanged(0, scope = scope, safeSet = true)
        var test by testObs
        val numRuns = 10000
        (0 until numRuns).map { scope.launch { testObs.changeValue { it + 1 } } }.joinAll()
        test isEqual numRuns
    }
}


fun Random.Default.nextInt(range: IntRange) = nextInt(range.first, range.last)
fun Random.Default.nextBoolean(percent: Float) = nextFloat() <= percent