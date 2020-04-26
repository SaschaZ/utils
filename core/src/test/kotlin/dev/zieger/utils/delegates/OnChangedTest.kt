package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.*
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test
import kotlin.random.Random

class OnChangedTest {

    private var calledCnt: Int = 0

    private var toTestOnChangeOldVar: Int? = null
    private var toTestOnChangeNewVar: Int? = null
    private var toTestOnChangePrevValues: List<Int?>? = null
    private var toTestOnChangedClearPrevValues: (() -> Unit)? = null

    private fun newDelegate(
        storeRecentValues: Boolean,
        notifyForExisting: Boolean,
        notifyOnChangedOnly: Boolean,
        scope: CoroutineScope?,
        mutex: Mutex?
    ) = OnChanged(
        0,
        storeRecentValues = storeRecentValues,
        notifyForExisting = notifyForExisting,
        notifyOnChangedValueOnly = notifyOnChangedOnly,
        scope = scope,
        mutex = mutex
    ) {
        calledCnt++
        toTestOnChangeOldVar = previousValue
        toTestOnChangeNewVar = value
        toTestOnChangePrevValues = previousValues
        toTestOnChangedClearPrevValues = clearPreviousValues
    }

    @Test
    fun testIt() = runTest {
        fun testWithValues(
            storePreviousValues: Boolean,
            notifyForExisting: Boolean,
            notifyOnChangedOnly: Boolean,
            scope: CoroutineScope?,
            mutex: Mutex?
        ) {
            calledCnt = 0
            toTestOnChangeOldVar = null
            toTestOnChangeNewVar = null
            toTestOnChangePrevValues = null
            toTestOnChangedClearPrevValues = null

            var toTestVar: Int by newDelegate(storePreviousValues, notifyForExisting, notifyOnChangedOnly, scope, mutex)
            var prevVal: Int? = 0
            var prevValues = ArrayList<Int?>().apply { add(0) }

            (1..100).forEach { i ->
                var newValue: Int
                do {
                    newValue = Random.nextInt()
                } while (i == 1 && newValue == 0)

                toTestVar = newValue
                toTestVar assert newValue % "$newValue|$i|V $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"
                calledCnt assert when {
                    notifyForExisting -> i + 1
                    else -> i
                } % "$newValue|$i|C $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"
                toTestOnChangeNewVar assert newValue % "$newValue|$i|N $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"
                toTestOnChangeOldVar assert prevVal % "$newValue|$i|O $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"
                toTestOnChangePrevValues assert when {
                    !storePreviousValues -> emptyList<Int?>()
                    notifyForExisting && i <= 50 -> listOf(0) + prevValues
                    else -> prevValues
                } % "$newValue|$i|P $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"

                if (i == 50) {
                    toTestOnChangedClearPrevValues?.invoke()
                    prevValues = ArrayList<Int?>()
                }
                prevVal = newValue
                prevValues.add(newValue)

            }
        }

        parameterMix(
            { OnChangedParams(it) },
            param("storePreviousValues", true, false),
            param("notifyForExisting", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", DefaultCoroutineScope(), null),
            param("mutex", Mutex(), null)
        ) {
            testWithValues(storePreviousValues, notifyForExisting, notifyOnChangedOnly, scope, mutex)
        }
    }.asUnit()

    data class OnChangedParams(val map: Map<String, ParamInstance<*>>) {

        val storePreviousValues: Boolean by bind(map)
        val notifyForExisting: Boolean by bind(map)
        val notifyOnChangedOnly: Boolean by bind(map)
        val scope: CoroutineScope? by bind(map)
        val mutex: Mutex? by bind(map)
    }
}