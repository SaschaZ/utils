package de.gapps.utils.log

import de.gapps.utils.misc.asUnit
import de.gapps.utils.misc.catch
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.minutes
import de.gapps.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking
import me.tongfei.progressbar.ProgressBar

object LogTestApp {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        catch(Unit) {
            ProgressBar("Test", 100).useRun {
                extraMessage = "Reading..."
                repeat(100) {
                    step()
                    delay(1.seconds)
                }
            }
        }

        delay(1.minutes)
    }.asUnit()
}

inline fun <T : AutoCloseable?, R> T.useRun(block: T.() -> R): R = use { run { block() } }
