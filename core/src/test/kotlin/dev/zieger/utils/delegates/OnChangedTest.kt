@file:Suppress("MemberVisibilityCanBePrivate", "UNUSED_ANONYMOUS_PARAMETER")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.*
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.coroutines.Continuation
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChangedTest.OnChangedResults.OnChangedTestResult
import dev.zieger.utils.delegates.OnChangedTest.OnChangedResults.Veto
import dev.zieger.utils.misc.DataClass
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.minutes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

private val mutex = Mutex()

fun fakeSuspend(block: suspend CoroutineScope.() -> Unit) = object : CoroutineScope {
    override val coroutineContext: CoroutineContext = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
    } //+ Job()
}.launchEx(mutex = mutex) { block() }

class OnChangedTest {

    sealed class OnChangedResults {
        data class OnChangedTestResult(
            val calledCount: Int = 0,
            val newVal: Int = 0,
            val newValBefore: Int? = null,
            val oldVal: Int? = null,
            val oldValBefore: Int? = null,
            val previousValues: List<Int?> = emptyList(),
            val clearPrevValues: () -> Unit = {},
            val isInitialNotification: Boolean = false
        ) : OnChangedResults()

        object Veto : OnChangedResults()
    }

    data class OnChangedParams(val map: Map<String, ParamInstance<*>>) : DataClass() {

        val newValueFactory: (Int?) -> Int by bind(map)

        val storePreviousValues: Boolean by bind(map)
        val notifyForInitial: Boolean by bind(map)
        val notifyOnChangedOnly: Boolean by bind(map)

        val scope: CoroutineScope? by bind(map)
        val mutex: Mutex? by bind(map)
        val veto: (Int) -> Boolean by bind(map)
        var vetoVal: Boolean = false
        val doClearPrevValues: (Int) -> Boolean by bind(map)
    }

    private var resultChannel = Channel<OnChangedResults>(Channel.UNLIMITED)
    private val results = ArrayList<OnChangedResults>()
    private var currentResult: OnChangedTestResult? = null
    private val testScope = DefaultCoroutineScope()
    private val continuation = Continuation()

    private fun OnChangedParams.newDelegate(
        initial: Int
    ) = OnChanged(
        initial,
        storeRecentValues = storePreviousValues,
        notifyForInitial = notifyForInitial,
        notifyOnChangedValueOnly = notifyOnChangedOnly,
        scope = scope,
        mutex = mutex,
        veto = {
            veto(it).also { r ->
                if (r && it != -1) {
                    vetoVal = r
                    testScope.launchEx {
                        resultChannel.send(Veto)
                        continuation.trigger()
                        println("trigger")
                    }
                } else {
                    vetoVal = false
                }
            }
        }
    ) {
        if (value == -1) resultChannel.close()
        else {
            currentResult = (currentResult ?: OnChangedTestResult())
                .copy(
                    newVal = value, oldVal = previousValue, previousValues = previousValues,
                    clearPrevValues = clearPreviousValues, isInitialNotification = isInitialNotification
                )
                .also { result ->
//                    if (!resultChannel.offer(result))
                    testScope.launchEx {
                        resultChannel.send(result)
                        println("trigger")
                        continuation.trigger()
//                        }
//                    else {
//                        testScope.launchEx {
//                            println("trigger")
//                            continuation.trigger()
//                        }
                    }
                }
        }
    }

    @Test
    fun testIt() = runTest(1.minutes) {
        suspend fun OnChangedParams.testWithValues(initial: Int): List<OnChangedResults> {
            println("Input:\n\t$this")
            resultChannel = Channel(Channel.UNLIMITED)
            val readJob = createReadJob()

            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
            var toTestVar: Int by newDelegate(initial)

            repeat(20) { i ->
                currentResult = OnChangedTestResult(
                    newValBefore = currentResult?.newVal,
                    oldValBefore = currentResult?.oldVal,
                    calledCount = i
                )
                toTestVar = newValueFactory(currentResult?.newValBefore)
                println("wait for trigger ($i)")
                continuation.suspendUntilTrigger()
            }
            toTestVar = -1

            resultChannel.close()
            readJob.join()
            return results
        }

        parameterMixCollect(
            { OnChangedParams(it) },
            param("newValueFactory", { old: Int? ->
                var result: Int? = null
                do {
                    result = Random.nextInt(0..9)
                } while (result == old)
                result
            }),
            param("storePreviousValues", true, false),
            param("notifyForInitial", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", TestCoroutineScope(), null),
            param("mutex", Mutex(), null),
            param("veto", { value: Int -> Random.nextBoolean(0.2f) }),
            param("doClearPrevValues", { value: Int -> Random.nextBoolean(0.05f) })
        ) {
//            testWithValues(0)
            Channel<OnChangedResults>()
        }.testResults()
    }.asUnit()

    private fun createReadJob(): Job = testScope.launchEx {
        results.clear()
        results.addAll(resultChannel.toList())
    }

    private suspend fun Map<OnChangedParams, Channel<OnChangedResults>>.testResults() {
        for ((input, outputs) in this) {
            outputs.toList().forEach { output ->
                println("Input:\n\t$input vetoVal=${input.vetoVal}\nOutput:\n\t$output\n")
                when (output) {
                    is OnChangedTestResult -> {
                        input.vetoVal assert false
                    }
                    is Veto -> {
                        input.vetoVal assert true
                    }
                }
            }
        }
    }
}

fun Random.Default.nextInt(range: IntRange) = nextInt(range.first, range.last)
fun Random.Default.nextBoolean(percent: Float) = nextFloat() <= percent
