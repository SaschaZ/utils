//@file:Suppress("MemberVisibilityCanBePrivate")
//
//package dev.zieger.utils.delegates
//
//import dev.zieger.utils.core_testing.*
//import dev.zieger.utils.core_testing.assertion.assert
//import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
//import dev.zieger.utils.misc.asUnit
//import dev.zieger.utils.misc.name
//import dev.zieger.utils.time.duration.seconds
//import jdk.nashorn.internal.ir.annotations.Ignore
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Runnable
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.sync.Mutex
//import org.junit.jupiter.api.Test
//import kotlin.coroutines.CoroutineContext
//
//class TestCoroutineScope : CoroutineScopeEx(TestCoroutineScope::class.name, object : CoroutineDispatcher() {
//    override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
//})
//
//class OnChangedTest {
//
//
//    data class OnChangedTestResult(
//        val calledCount: Int = 0,
//        val newVar: Int? = null,
//        val newVarBefore: Int? = null,
//        val oldVar: Int? = null,
//        val oldVarBefore: Int? = null,
//        val previousValues: List<Int?> = emptyList(),
//        val clearPrevValues: (() -> Unit)? = null,
//        val isInitialNotification: Boolean = false
//    )
//
//    private val resultChannel = Channel<OnChangedTestResult>(Channel.UNLIMITED)
//    private var currentResult: OnChangedTestResult? = null
//
//    private fun newDelegate(
//        initial: Int,
//        storeRecentValues: Boolean,
//        notifyForExisting: Boolean,
//        notifyOnChangedOnly: Boolean,
//        scope: CoroutineScope?,
//        mutex: Mutex?,
//        veto: (Int) -> Boolean
//    ) = OnChanged(
//        initial,
//        storeRecentValues = storeRecentValues,
//        notifyForInitial = notifyForExisting,
//        notifyOnChangedValueOnly = notifyOnChangedOnly,
//        scope = scope,
//        mutex = mutex,
//        veto = veto
//    ) {
//        currentResult = (currentResult ?: OnChangedTestResult())
//            .copy(
//                newVar = value, oldVar = previousValue, previousValues = previousValues,
//                clearPrevValues = clearPreviousValues, isInitialNotification = isInitialNotification
//            )
//            .also { resultChannel.offer(it) }
//    }
//
//    @Test
//    fun testIt() = runTest(120.seconds) {
//        suspend fun testWithValues(
//            initial: Int,
//            storePreviousValues: Boolean,
//            notifyForInitial: Boolean,
//            notifyOnChangedOnly: Boolean,
//            scope: CoroutineScope?,
//            mutex: Mutex?,
//            vetoReturn: Boolean,
//            veto: (Int) -> Boolean
//        ) {
//
//            var toTestVar: Int by newDelegate(
//                initial,
//                storePreviousValues,
//                notifyForInitial,
//                notifyOnChangedOnly,
//                scope,
//                mutex,
//                veto
//            )
//            var prevVal: Int? = null
//            var prevValues = ArrayList<Int?>().apply { add(initial) }
//
//            val num = 100
//            (1..num).forEach { newValue ->
//                currentResult =
//                    OnChangedTestResult(newVarBefore = currentResult?.newVar, oldVarBefore = currentResult?.oldVar)
//                toTestVar = newValue
//
//                for (result in resultChannel) {
//                    result.run {
//                        toTestVar assert (if (vetoReturn) initial else newValue)
//
//                        calledCount assert when {
//                            vetoReturn -> if (notifyForInitial) 1 else 0
//                            notifyForInitial -> newValue + 1
//                            else -> newValue
//                        }
//                        newValue assert when {
//                            vetoReturn -> if (notifyForInitial) initial else null
//                            notifyForInitial && newValue == 1 -> newValue
//                            else -> newValue
//                        }
//                        oldVar assert when {
//                            vetoReturn -> null
//                            newValue == 1 && notifyForInitial -> initial
//                            else -> prevVal
//                        }
//                        prevValues assert when {
//                            vetoReturn -> if (notifyForInitial) emptyList() else null
//                            !storePreviousValues -> emptyList<Int?>()
//                            else -> prevValues.filterNotNull()
//                        }
//
//                        if (newValue == num / 2) {
//                            clearPrevValues?.invoke()
//                            prevValues = ArrayList<Int?>()
//                        }
//                        prevVal = newValue
//                        prevValues.add(newValue)
//                    }
//                }
//            }
//        }
//
//        parameterMix(
//            { OnChangedParams(it) },
//            param("storePreviousValues", true, false),
//            param("notifyForInitial", true, false),
//            param("notifyOnChangedOnly", true, false),
//            param("scope", TestCoroutineScope(), null),
//            param("mutex", Mutex(), null),
//            param("vetoReturn", true, false)
//        ) {
//            println(this)
//            testWithValues(
//                0, storePreviousValues, notifyForInitial, notifyOnChangedOnly, scope, mutex,
//                vetoReturn, veto
//            )
//        }
//    }.asUnit()
//
//    data class OnChangedParams(val map: Map<String, ParamInstance<*>>) {
//
//        val storePreviousValues: Boolean by bind(map)
//        val notifyForInitial: Boolean by bind(map)
//        val notifyOnChangedOnly: Boolean by bind(map)
//
//        val scope: CoroutineScope? by bind(map)
//        val mutex: Mutex? by bind(map)
//        val vetoReturn: Boolean by bind(map)
//        val veto: (Int) -> Boolean
//            get() = { vetoReturn }
//    }
//}