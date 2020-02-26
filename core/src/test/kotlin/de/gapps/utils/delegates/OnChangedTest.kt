package de.gapps.utils.delegates

import de.gapps.utils.misc.asUnit
import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.assertion.onFail
import de.gapps.utils.testing.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OnChangedTest {

    private var calledCnt: Int = 0
    private lateinit var delegate: OnChanged<Int>

    private var toTestOnChangeOldVar: Int? = null
    private var toTestOnChangeNewVar: Int? = null

    @BeforeEach
    fun before() {
        calledCnt = 0
        toTestOnChangeOldVar = null
        toTestOnChangeNewVar = null

        delegate = OnChanged(0) {
            calledCnt++
            toTestOnChangeOldVar = previousValue
            toTestOnChangeNewVar = value
        }
    }

    @Test
    fun testIt() = runTest {
        var toTestVar: Int by delegate

        toTestVar onFail "1" assert 0
        calledCnt onFail "1C" assert 0
        toTestOnChangeNewVar onFail "1" assert null
        toTestOnChangeOldVar onFail "1" assert null

        toTestVar = 0
        toTestVar onFail "2" assert 0
        calledCnt onFail "2C" assert 0
        toTestOnChangeNewVar onFail "2" assert null
        toTestOnChangeOldVar onFail "2" assert null

        toTestVar = 1
        toTestVar onFail "3" assert 1
        calledCnt onFail "3C" assert 1
        toTestOnChangeNewVar onFail "3" assert 1
        toTestOnChangeOldVar onFail "3" assert 0

        toTestVar = 2
        toTestVar onFail "4" assert 2
        calledCnt onFail "4C" assert 2
        toTestOnChangeNewVar onFail "4" assert 2
        toTestOnChangeOldVar onFail "4" assert 1
    }.asUnit()
}