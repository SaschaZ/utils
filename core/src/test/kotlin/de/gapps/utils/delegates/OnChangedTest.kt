package de.gapps.utils.delegates

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.rem
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.misc.asUnit
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

        toTestVar assert 0 % "1"
        calledCnt assert 0 % "1C"
        toTestOnChangeNewVar assert null % "1"
        toTestOnChangeOldVar assert null % "1"

        toTestVar = 0
        toTestVar assert 0 % "2"
        calledCnt assert 0 % "2C"
        toTestOnChangeNewVar assert null % "2"
        toTestOnChangeOldVar assert null % "2"

        toTestVar = 1
        toTestVar assert 1 % "3"
        calledCnt assert 1 % "3C"
        toTestOnChangeNewVar assert 1 % "3"
        toTestOnChangeOldVar assert 0 % "3"

        toTestVar = 2
        toTestVar assert 2 % "4"
        calledCnt assert 2 % "4C"
        toTestOnChangeNewVar assert 2 % "4"
        toTestOnChangeOldVar assert 1 % "4"
    }.asUnit()
}