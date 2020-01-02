package de.gapps.utils.log

import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.milliseconds
import de.gapps.utils.time.duration.minutes
import kotlinx.coroutines.runBlocking


object LogTestApp {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        //        catch(Unit) {
//            ProgressBar("Test", 100).useRun {
//                extraMessage = "Reading..."
//                repeat(100) {
//                    step()
//                    delay(1.seconds)
//                }
//            }
//        }

        print("\u001b[35mHello")
        print("\u001b[36mWorld")
        delay(500.milliseconds)
        print(13.toChar())
//        (0 until 5).forEach { print(8.toChar()) }
        System.out.flush()
        print("Foo")

        delay(1.minutes)
    }.asUnit()
}

inline fun <T : AutoCloseable?, R> T.useRun(block: T.() -> R): R = use { run { block() } }
