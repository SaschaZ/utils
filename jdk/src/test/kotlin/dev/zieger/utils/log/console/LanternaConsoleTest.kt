package dev.zieger.utils.log.console

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class LanternaConsoleTest {

    @Test
    fun testConsole() = runBlocking {
        LanternaConsole().scope {
        }
    }
}