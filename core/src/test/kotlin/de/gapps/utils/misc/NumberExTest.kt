package de.gapps.utils.misc

import org.junit.jupiter.api.Test

class NumberExTest {

    @Test
    fun testIt() {
        val someDouble = 5.5
        val someFloat = 3.4f
        val someLong = 100L
        val someShort: Short = 10
        val someByte: Byte = 5.toByte()
        val someNumber: Number = 5
        val result = someDouble + someFloat + someLong / someShort + someByte + someNumber * someShort
        println("result=$result; type=${result.javaClass.simpleName}")
    }

    @Test
    fun testNumberExToNumberCast() {
        val nEx = 10.ex
        val n = nEx as Number
        assert(nEx == 10.ex)
        assert(n == 10)
    }
}

private operator fun Double.plus(number: Number): Double {
    return this + number.toDouble()
}
