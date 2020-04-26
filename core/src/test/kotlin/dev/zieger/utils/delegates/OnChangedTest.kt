package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.misc.asUnit
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
        notifyOnChangedOnly: Boolean
    ) = OnChanged(
        0,
        storeRecentValues = storeRecentValues,
        notifyForExisting = notifyForExisting,
        notifyOnChangedValueOnly = notifyOnChangedOnly
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
            notifyOnChangedOnly: Boolean
        ) {
            calledCnt = 0
            toTestOnChangeOldVar = null
            toTestOnChangeNewVar = null
            toTestOnChangePrevValues = null
            toTestOnChangedClearPrevValues = null

            var toTestVar: Int by newDelegate(storePreviousValues, notifyForExisting, notifyOnChangedOnly)
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

        testWithValues(storePreviousValues = true, notifyForExisting = false, notifyOnChangedOnly = true)
        testWithValues(storePreviousValues = false, notifyForExisting = false, notifyOnChangedOnly = true)
        testWithValues(storePreviousValues = false, notifyForExisting = false, notifyOnChangedOnly = false)
        testWithValues(storePreviousValues = true, notifyForExisting = true, notifyOnChangedOnly = true)
        testWithValues(storePreviousValues = false, notifyForExisting = true, notifyOnChangedOnly = true)
        testWithValues(storePreviousValues = true, notifyForExisting = false, notifyOnChangedOnly = false)
    }.asUnit()
}