@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "BooleanLiteralArgument")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.param
import dev.zieger.utils.core_testing.parameterMixCollect
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChangedTest.OnChangedParams
import dev.zieger.utils.delegates.OnChangedTestNew.OnChangedResultsNew.OnChangedTestResultInput
import dev.zieger.utils.delegates.OnChangedTestNew.OnChangedResultsNew.OnChangedTestResultOutput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test
import kotlin.random.Random

class OnChangedTestNew {

    val testScope = DefaultCoroutineScope()

    sealed class OnChangedResultsNew {
        data class OnChangedTestResultInput(
            val inputValue: TestValueContainer,
            val veto: Boolean,
            val clearPrevious: Boolean
        ) : OnChangedResultsNew()

        data class OnChangedTestResultOutput(
            val newValue: Int,
            val propertyValue: Int,
            val newVeto: Boolean,
            val previousCleared: Boolean,
            val onChangedScope: IOnChangedScope<TestValueContainer>
        ) : OnChangedResultsNew()
    }

    data class TestValueContainer(
        val value: Int = -1,
        val idx: Int = -1
    ) {
        override fun equals(other: Any?): Boolean = value == (other as? TestValueContainer)?.value
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "[$value - $idx]"
    }

    @Test
    fun testOnChanged() = runTest {
        parameterMixCollect(
            { OnChangedParams(it) },
            param("newValueFactory", { old: Int? -> Random.nextInt(0..9) }),
            param("storePreviousValues", true, false),
            param("notifyForInitial", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", DefaultCoroutineScope(), null),
            param("mutex", Mutex(), null),
            param("veto", { value: Int -> Random.nextBoolean(0.1f) }),
            param("doClearPrevValues", { value: Int -> Random.nextBoolean(0.1f) })
        ) {
            var newValue: Int = -1
            var newVeto = false
            var newClearCache = false
            val results = Channel<OnChangedResultsNew>(Channel.UNLIMITED)

            var propertyValue: () -> TestValueContainer = { TestValueContainer() }
            var testProperty by OnChanged(TestValueContainer(), storePreviousValues, notifyForInitial,
                notifyOnChangedOnly, scope, mutex, { newVeto }) { v ->
                if (newClearCache) clearPreviousValues()
                results.offer(
                    OnChangedTestResultOutput(
                        newValue, propertyValue().value, newVeto, newClearCache,
                        OnChangedScope(
                            value, thisRef, previousValue, ArrayList(previousValues),
                            clearPreviousValues, isInitialNotification
                        )
                    )
                )
            }
            propertyValue = { testProperty }

            repeat(200) { i ->
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

    private suspend fun Map<OnChangedParams, Channel<OnChangedResultsNew>>.verify() {
        delay(1000)

        for ((input, result) in this) {
            println("\n\n$input")

            var prevInIdx = -1
            var prevOutIdx = if (input.notifyForInitial) -2 else -1
            var directInput = OnChangedTestResultInput(TestValueContainer(), false, false)
            val vetoUpComing = ArrayList<Int>()
            val sameValueUpComing = ArrayList<Int>()
            var isFirstNotification = true

            result.close()
            result.toList()/*.sortedBy { it.onChangedScope.value.idx }*/.forEach { r ->
                when (r) {
                    is OnChangedTestResultInput -> {
                        println(r)

                        val idx = r.inputValue.idx
                        (idx - prevInIdx) assert 1
                        if (r.veto) vetoUpComing.add(idx)
                        else if (r.inputValue.value == directInput.inputValue.value && input.notifyOnChangedOnly)
                            sameValueUpComing.add(idx)
                        else directInput = r

                        prevInIdx = idx
                    }
                    is OnChangedTestResultOutput -> {
                        println("\t$r")

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
