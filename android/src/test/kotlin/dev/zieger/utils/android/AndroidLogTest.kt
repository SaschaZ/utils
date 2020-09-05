@file:Suppress("unused")

package dev.zieger.utils.android

import dev.zieger.utils.log.Log
import dev.zieger.utils.misc.cast
import org.junit.jupiter.api.Test
import java.io.PrintStream

class AndroidLogTest {

    @Test
    fun testLog() {
        var chars = ""
        val orig = System.out
        System.setOut(object : PrintStream(orig, true) {
            override fun println(x: Any?) {
                chars += "${x?.cast<String>()}\n"
//                super.println(x)
            }
        })

        AndroidLog.initialize("fooboo", addTime = false, addCallOrigin = false, useSystemOut = true)
        Log.i("this is a test")
        println("this is a test")

        System.setOut(orig)
        println("length: ${chars.length} - $chars")
    }
}