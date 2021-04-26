@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "BooleanLiteralArgument")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.isFalse
import dev.zieger.utils.core_testing.assertion2.isTrue
import dev.zieger.utils.core_testing.assertion2.rem
import dev.zieger.utils.core_testing.mix.ParamInstance
import dev.zieger.utils.core_testing.mix.bind
import dev.zieger.utils.core_testing.mix.param
import dev.zieger.utils.core_testing.mix.parameterMixCollect
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.misc.joinToStringIndexed
import dev.zieger.utils.misc.name
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.runEachIndexed
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.lang.Integer.max
import kotlin.random.Random

data class OnChangedTestParams(val params: Map<String, ParamInstance<*>>) {

    val initial: Int by bind(params)
    val inputs: List<Int> by bind(params)

    val storePreviousValues: Boolean by bind(params)
    val notifyForInitial: Boolean by bind(params)
    val notifyOnChangedOnly: Boolean by bind(params)

    val coroutineScope: CoroutineScope? by bind(params)
    val mutex: Mutex by bind(params)
    val safeSet: Boolean by bind(params)

    val map: List<Int?> by bind(params)
    val veto: List<Boolean> by bind(params)

    val doClearPrevValues: List<Boolean> by bind(params)

    override fun toString(): String = "${this::class.name}(\n" +
            "\tinitial: $initial\n" +
            "\tinputs: ${inputs.joinToStringIndexed { idx, value -> "$idx: $value" }}\n" +
            "\tstorePreviousValues: $storePreviousValues\n" +
            "\tnotifyForInitial: $notifyForInitial\n" +
            "\tnotifyOnChangedOnly: $notifyOnChangedOnly\n" +
            "\tcoroutineScope: ${coroutineScope?.let { it::class.name }}\n" +
            "\tmutex: $mutex\n" +
            "\tsafeSet: $safeSet\n" +
            "\tmap: ${map.joinToStringIndexed { idx, value -> "$idx: $value" }}\n" +
            "\tveto: ${veto.joinToStringIndexed { idx, value -> "$idx: $value" }}\n" +
            "\tdoClearPrevious: ${doClearPrevValues.joinToStringIndexed { idx, value -> "$idx: $value" }})"
}

class TestValueContainer(
    val value: Int = -1,
    val idx: Int = -1
) {
    override fun equals(other: Any?): Boolean =
        value == (other as? TestValueContainer)?.value || value == (other as? Int)

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "[$value - $idx]"
}

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
    override fun toString(): String =
        "OnChangedResult(\nisSuspended: $isSuspended\nparams: $params\n" +
                "rawInput: $rawInput\nnewValue: $newValue\npropertyValue: $propertyValue\n" +
                "veto: $veto\npreviousCleared: $previousCleared\nonChangedScope: $onChangedScope)"
}

class TestContext {

    companion object {

        private const val INPUT_VALUE_NUM = 10
    }

    fun <P : Any?, S : IOnChangedScopeWithParent<P, TestValueContainer>> S.onChangedListenerBlock(
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
                isSuspended,
                params,
                rawInputValue,
                newValue,
                propertyValue,
                newVeto,
                newClearCache,
                copy()
            )
        )
    }

    private fun IOnChangedScopeWithParent<Any?, TestValueContainer>.copy() =
        OnChangedScopeWithParent(
            value, parent, propertyName, previousValue, ArrayList(previousValues),
            clearPreviousValues, isInitialNotification, valueChangedListener
        )

    suspend fun Map<OnChangedTestParams, Channel<OnChangedResult>>.verify() {
        delay(1000)

        toList().forEachIndexed { inputIdx, (input, result) ->
            var prevInIdx = -1

            result.close()
            result.toList().sortedBy { it.newValue.idx }.forEachIndexed { i, r ->
                println("\n\nloopIdx: $inputIdx\nresult: $r\n")

                val idx = r.newValue.idx
                idx isEqual (prevInIdx + getIdxDiff(r, input, idx, prevInIdx)) % "IDX"

                r.newValue.value isEqual (if (r.onChangedScope.isInitialNotification) input.initial else input.map[idx]
                    ?: input.inputs[idx]) % "VALUE"
                r.onChangedScope.previousValue?.value isEqual when {
                    r.onChangedScope.isInitialNotification -> null
                    idx == 0 -> input.initial
                    else -> (idx - getIdxDiff(r, input, idx, prevInIdx)).let {
                        input.map.getOrNull(it) ?: input.inputs.getOrNull(it) ?: input.initial
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
                    (veto[it] || notifyOnChangedOnly && value == prev).also { suppressed ->
                        if (!suppressed) prev = value
                    }
                }
            }

    fun parameter(block: OnChangedTestParams.() -> Channel<OnChangedResult>): Map<OnChangedTestParams, Channel<OnChangedResult>> {
        var safeSet = false

        return parameterMixCollect(
            { OnChangedTestParams(it) },
            param(OnChangedTestParams::initial, -1),
            param(OnChangedTestParams::inputs, 1) {
                (0..INPUT_VALUE_NUM).map { Random.nextInt(0..9) }
            },
            param(OnChangedTestParams::map, 1) {
                (0..INPUT_VALUE_NUM).map {
                    Random.nextInt(0..9).nullWhen { Random.nextBoolean(0.9f) }
                }
            },
            param(OnChangedTestParams::veto, 1) {
                (0..INPUT_VALUE_NUM).map { Random.nextBoolean(0.1f) }
            },
            param(OnChangedTestParams::doClearPrevValues, 1) {
                (0..INPUT_VALUE_NUM).map { Random.nextBoolean(0.1f) }
            },
            param(OnChangedTestParams::storePreviousValues, true, false),
            param(OnChangedTestParams::notifyForInitial, true, false),
            param(OnChangedTestParams::notifyOnChangedOnly, true, false),
            param(OnChangedTestParams::coroutineScope, 2) {
                if (safeSet) DefaultCoroutineScope() else null
            },
            param(OnChangedTestParams::mutex, Mutex()),
            param(OnChangedTestParams::safeSet, 2) { safeSet.also { safeSet = !it } }
        ) { block() }
    }
}

class OnChangedTest : FunSpec({

    var ctx = TestContext()

    beforeEach {
        ctx = TestContext()
    }

    test("onChanged") {
        ctx.run {
            parameter {
                var newValue = 0
                var newVeto = false
                var newMap: Int? = null
                var newClearCache = false
                val results = Channel<OnChangedResult>(Channel.UNLIMITED)

                var propertyValue: () -> TestValueContainer = { TestValueContainer() }
                var testProperty by OnChanged(
                    OnChangedParams(
                        initial = TestValueContainer(initial),
                        storeRecentValues = storePreviousValues,
                        notifyForInitial = notifyForInitial,
                        notifyOnChangedValueOnly = notifyOnChangedOnly,
                        scope = coroutineScope,
                        mutex = mutex,
                        map = { newMap?.let { nm -> TestValueContainer(nm, it.idx) } ?: it },
                        veto = { newVeto },
                        onChangedS = coroutineScope?.let {
                            { v ->
                                onChangedListenerBlock(
                                    true, this@parameter, newValue, v, results,
                                    propertyValue(), newVeto, newClearCache
                                )
                            }
                        },
                        onChanged = if (coroutineScope == null) {
                            { v ->
                                onChangedListenerBlock(
                                    false, this@parameter, newValue, v, results,
                                    propertyValue(), newVeto, newClearCache
                                )
                            }
                        } else null
                    )
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
    }

    test("onChangedWithParent") {
        ctx.run {
            parameter {
                var newValue: Int = -1
                var newVeto = false
                var newMap: Int? = null
                var newClearCache = false
                val results = Channel<OnChangedResult>(Channel.UNLIMITED)

                var propertyValue: () -> TestValueContainer = { TestValueContainer() }
                var testProperty by OnChangedWithParent(
                    OnChangedParamsWithParent(
                        TestValueContainer(initial),
                        storeRecentValues = storePreviousValues,
                        notifyForInitial = notifyForInitial,
                        notifyOnChangedValueOnly = notifyOnChangedOnly,
                        scope = coroutineScope,
                        mutex = mutex,
                        veto = { newVeto },
                        map = { newMap?.let { nm -> TestValueContainer(nm, it.idx) } ?: it },
                        onChangedS = coroutineScope?.let {
                            { v ->
                                onChangedListenerBlock(
                                    true, this@parameter, newValue, v, results,
                                    propertyValue(), newVeto, newClearCache
                                )
                            }
                        },
                        onChanged = if (coroutineScope == null) {
                            { v ->
                                onChangedListenerBlock(
                                    false, this@parameter, newValue, v, results,
                                    propertyValue(), newVeto, newClearCache
                                )
                            }
                        } else null
                    )
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
    }

    test("small test") {
        ctx.run {
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
    }

    test("change value") {
        ctx.run {
            val scope = IoCoroutineScope()
            val testObs = OnChanged(0, scope = scope)
            var test by testObs
            val numRuns = 10000
            (0 until numRuns).map { scope.launch { testObs.changeValue { it + 1 } } }.joinAll()
            test isEqual numRuns
        }
    }

    test("suspend until") {
        val testPropertyObs = OnChanged<Int?>(null)
        var testProperty by testPropertyObs

        var caught = false
        val suspendUntilJob = launchEx(onCatch = { caught = true}, exclude = emptyList()) {
            testPropertyObs.suspendUntil(3, 1.seconds) { it isEqual 3 }
        }
        testProperty = 1
        testProperty isEqual 1
        caught.isFalse()
        delay(250.milliseconds)
        suspendUntilJob.isActive.isTrue()
        testProperty = null
        testProperty isEqual null
        caught.isFalse()
        delay(250.milliseconds)
        suspendUntilJob.isActive.isTrue()
        testProperty = 3
        testProperty isEqual 3
        caught.isFalse()
        delay(250.milliseconds)
        suspendUntilJob.isActive.isFalse()

        testPropertyObs.suspendUntil(3) { it isEqual 3 }

        caught = false
        launchEx(onCatch = { caught = true }, exclude = emptyList()) {
            testPropertyObs.suspendUntil(4, 100.milliseconds) { it isEqual 4 }
        }
        delay(250.milliseconds)
        caught.isTrue()
    }

    test("next change") {
        val testPropertyObs = OnChanged<Int?>(null)
        var testProperty by testPropertyObs

        var caught = false
        val nextChangeJob = launchEx(onCatch = { caught = true }, exclude = emptyList()) {
            testPropertyObs.nextChange(200.milliseconds) { it isEqual 3 }
        }

        delay(100.milliseconds)
        nextChangeJob.isActive.isTrue()
        caught.isFalse()
        testProperty = 3
        testProperty isEqual 3
        delay(500.milliseconds)
        caught.isFalse("caught")
        nextChangeJob.isActive.isFalse("next change job")
    }
})


fun Random.Default.nextInt(range: IntRange) = nextInt(range.first, range.last)
fun Random.Default.nextBoolean(percent: Float) = nextFloat() <= percent