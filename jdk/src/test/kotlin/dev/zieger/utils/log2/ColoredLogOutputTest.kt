package dev.zieger.utils.log2

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class ColoredLogOutputTest {

    @Test
    fun testColoredLog() = runBlocking {
        Log.output = ColoredLogOutput()

        Log.v("test verbose")
        Log.d("debug also")
        Log.i("and info")
        Log.w("followed by warning")
        Log.e("finished by exception")
    }
}