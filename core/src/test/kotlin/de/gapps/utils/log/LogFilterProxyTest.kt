package de.gapps.utils.log

import org.junit.Test

internal class LogFilterProxyTest {

    @Test
    fun testMessageWrapper() {
        println("before test")
        Log.v("das ist ein test")
    }
}