//@file:Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter", "RemoveExplicitTypeArguments")
//
//package dev.zieger.utils.observables
//
//import dev.zieger.utils.core_testing.*
//import dev.zieger.utils.core_testing.assertion.assert
//import dev.zieger.utils.core_testing.assertion.rem
//import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
//import dev.zieger.utils.misc.asUnit
//import dev.zieger.utils.observable.IObservableWithParent
//import dev.zieger.utils.observable.Observable
//import dev.zieger.utils.observable.ObservableParams
//import dev.zieger.utils.observable.ObservableParamsWithParent
//import io.kotlintest.specs.AnnotationSpec
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.sync.Mutex
//
//class ObservableTest : AnnotationSpec() {
//
//    private inline fun <T> params(
//        inputFactory: (Map<String, ParamInstance<*>>) -> T,
//        factory: T.() -> Unit
//    ) = parameterMix(
//        inputFactory,
//        param("storePreviousValues", true, false),
//        param("notifyForExisting", true, false),
//        param("notifyOnChangedOnly", true, false),
//        param("scope", DefaultCoroutineScope()),
//        param("mutex", Mutex()),
//        block = factory
//    )
//
//    @Test
//    fun testObservable() = runTestForInputFactory {
//        TestClass("foo", it) { initial, notifyOnChangedOnly, notifyForExisting,
//                               storePreviousValues, scope, mutex ->
//            Observable(
//                ObservableParamsWithParent(
//                    initial, notifyOnChangedValueOnly = notifyOnChangedOnly,
//                    notifyForInitial = notifyForExisting,
//                    storeRecentValues = storePreviousValues, scope = scope, mutex = mutex
//                )
//            )
//        }
//    }
//
//    @Test
//    fun testObservable2() = runTestForInputFactory {
//        TestClass("foo", it) { initial, notifyOnChangedOnly, notifyForExisting,
//                               storePreviousValues, scope, mutex ->
//            Observable<String>(
//                ObservableParams(
//                    initial,
//                    notifyOnChangedValueOnly = notifyOnChangedOnly,
//                    notifyForInitial = notifyForExisting,
//                    storeRecentValues = storePreviousValues,
//                    scope = scope,
//                    mutex = mutex
//                )
//            )
//        }
//    }
//
//    private fun <O : IObservableWithParent<Any?, String>> runTestForInputFactory(
//        inputFactory: (Map<String, ParamInstance<*>>) -> TestClass<String, O>
//    ) = runTest {
//        params(inputFactory) {
////            println(this)
//
//            val testClass = this
//            testClass.observable.value assert "foo" % "initial observable.value"
//            testClass.value assert "foo" % "initial value"
//
//            var latestObservedChanged: String? = null
//            testClass.observable.observe { /*Log.d("observe -> value=$it");*/ latestObservedChanged = it }
//            latestObservedChanged assert (if (notifyForExisting) "foo" else null) % "after observe without changes"
//
//            var latestObservedChangedS: String? = null
//            testClass.observable.observeS { /*Log.d("observeS -> value=$it");*/ latestObservedChangedS = it }
//            delay(10L)
//            latestObservedChangedS assert (if (notifyForExisting && scope != null) "foo" else null) % "after observe without changes suspended"
//
//            testClass.value = "boo"
//            delay(10L)
//
//            testClass.observable.value assert "boo" % "after change observable.value"
//            testClass.value assert "boo" % "after change value"
//            latestObservedChanged assert "boo" % "after change observe"
//            latestObservedChangedS assert (if (scope != null) "boo" else null) % "after change observe suspended"
//        }
//    }.asUnit()
//
//    companion object {
//
//        data class TestClass<T, O : IObservableWithParent<Any?, T>>(
//            val initial: String,
//            val map: Map<String, ParamInstance<*>>,
//            val factory: (String, Boolean, Boolean, Boolean, CoroutineScope?, Mutex?) -> O
//        ) {
//
//            val storePreviousValues: Boolean by bind(map)
//            val notifyForExisting: Boolean by bind(map)
//            val notifyOnChangedOnly: Boolean by bind(map)
//            val scope: CoroutineScope by bind(map)
//            val mutex: Mutex by bind(map)
//
//            val observable = factory(
//                initial, notifyOnChangedOnly, notifyForExisting,
//                storePreviousValues, scope, mutex
//            )
//            var value: T by observable
//        }
//    }
//}