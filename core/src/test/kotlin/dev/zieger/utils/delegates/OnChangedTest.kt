@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.*
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class OnChangedTest {

    private var calledCnt = AtomicInteger(0)

    private var toTestOnChangeOldVar: Int? = null
    private var toTestOnChangeNewVar: Int? = null
    private var toTestOnChangePrevValues: List<Int?>? = null
    private var toTestOnChangedClearPrevValues: (() -> Unit)? = null

    private fun newDelegate(
        storeRecentValues: Boolean,
        notifyForExisting: Boolean,
        notifyOnChangedOnly: Boolean,
        scope: CoroutineScope?,
        mutex: Mutex?,
        veto: (Int) -> Boolean
    ) = OnChanged(
        0,
        storeRecentValues = storeRecentValues,
        notifyForExisting = notifyForExisting,
        notifyOnChangedValueOnly = notifyOnChangedOnly,
        scope = scope,
        mutex = mutex,
        vetoP = veto
    ) {
        calledCnt.incrementAndGet()
        toTestOnChangeOldVar = previousValue
        toTestOnChangeNewVar = value
        toTestOnChangePrevValues = previousValues
        toTestOnChangedClearPrevValues = clearPreviousValues
    }

    @Test
    fun testIt() = runTest(120.seconds) {
        suspend fun testWithValues(
            storePreviousValues: Boolean,
            notifyForExisting: Boolean,
            notifyOnChangedOnly: Boolean,
            scope: CoroutineScope?,
            mutex: Mutex?,
            vetoReturn: Boolean,
            veto: (Int) -> Boolean
        ) {
            calledCnt.set(0)
            toTestOnChangeOldVar = null
            toTestOnChangeNewVar = null
            toTestOnChangePrevValues = null
            toTestOnChangedClearPrevValues = null

            var toTestVar: Int by newDelegate(
                storePreviousValues,
                notifyForExisting,
                notifyOnChangedOnly,
                scope,
                mutex,
                veto
            )
            var prevVal: Int? = 0
            var prevValues = ArrayList<Int?>().apply { add(0) }

            val num = 10
            (1..num).forEach { i ->
                var newValue: Int
                do {
                    newValue = Random.nextInt()
                } while (i == 1 && newValue == 0 || newValue == prevVal)

                toTestVar = newValue
                delay(10)
                toTestVar assert (if (vetoReturn) 0 else newValue) % "$newValue|$i|V $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"
                calledCnt.get() assert when {
                    vetoReturn -> 0
                    notifyForExisting -> i + 1
                    else -> i
                } % "$newValue|$i|C $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"

                toTestOnChangeNewVar assert (if (vetoReturn) null else newValue) % "$newValue|$i|N $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"
                toTestOnChangeOldVar assert (if (vetoReturn) null else prevVal) % "$newValue|$i|O $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"
                toTestOnChangePrevValues assert when {
                    vetoReturn -> null
                    !storePreviousValues -> emptyList<Int?>()
                    notifyForExisting && i <= num / 2 -> listOf(0) + prevValues
                    else -> prevValues
                } % "$newValue|$i|P $storePreviousValues|$notifyForExisting|$notifyOnChangedOnly"

                if (i == num / 2) {
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
            param("mutex", Mutex(), null),
            param("vetoReturn", true, false)
        ) {
            println(this)
            testWithValues(storePreviousValues, notifyForExisting, notifyOnChangedOnly, scope, mutex, vetoReturn, veto)
        }
    }.asUnit()

    data class OnChangedParams(val map: Map<String, ParamInstance<*>>) {

        val storePreviousValues: Boolean by bind(map)
        val notifyForExisting: Boolean by bind(map)
        val notifyOnChangedOnly: Boolean by bind(map)
        val scope: CoroutineScope? by bind(map)
        val mutex: Mutex? by bind(map)
        val vetoReturn: Boolean by bind(map)
        val veto: (Int) -> Boolean
            get() = { vetoReturn }
    }
}