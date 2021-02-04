package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor.ANSI.*
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.gui.console.progress.PROGRESS
import dev.zieger.utils.gui.console.progress.ProgressEntity.*
import dev.zieger.utils.gui.console.progress.ProgressSource
import dev.zieger.utils.gui.console.progress.ProgressUnit.Bytes
import dev.zieger.utils.gui.console.progress.RemoveWhen
import dev.zieger.utils.log2.Log
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.minutes
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.Job
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.random.Random

@Disabled
internal class LanternaConsoleTest {

    @Test
    fun testSimple() = runTest(1.minutes) {
        console { outnl(+"Hallo Ballo"); delay(30.seconds) }
    }

    @Test
    fun testComplex() = runTest(10.minutes) {
        console {
            Log.v("showing console …")
            outnl(+"FooBooFo°\b\n°\boBooFooBooFoFoFooBFooFFoFooBoooBooFooBooooBooBoooFFooBooooBooFooBoooFoFooFooBooBooFFooBooooBoooBoooBoooBooFooBooFooBoo")
            outnl(+"mooo\t\tbooo")
            outnl(+"määäää", +"foo" * GREEN / YELLOW, text { "boo" } * { RED } / { WHITE }, +"blub" * BLUE)

            var startIdx = 0
            var printed = 0
            var job: Job? = null
            outnl(text {
                startIdx++
                if (printed == 0) {
                    job = job ?: launchEx(interval = 1.seconds, delayed = 1.seconds) { printed++; refresh() }
                    " boofoo$printed "
                } else "  DU HURENSOHN$printed  "
            })
            repeat(500) {
                out(
                    text { "${it + startIdx}|" } *
                            { if (Random.nextBoolean()) YELLOW else GREEN } /
                            { BLACK }
                )
                delay(1.milliseconds)
            }
            outnl()

            outnl(+"hey")
            outnl(+"du")
            outnl(+"krasser")
            outnl(text { "typ :-*" } * { if (Random.nextBoolean()) YELLOW else GREEN }, offset = 1)

            ProgressSource(this@runTest, total = 500, unit = Bytes).run {
                outnl(*PROGRESS(this, Bar(), Space, DoneOfTotal(), RemoveWhen(1.0)))
                launchEx(interval = 50.milliseconds) { done += 1 }
            }

            outnl(*SysInfo())

            delay(50.seconds)
        }
    }.asUnit()
}