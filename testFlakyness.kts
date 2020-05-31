#!/usr/bin/env kscript
//DEPS io.ktor:ktor-client-apache:1.3.0,io.ktor:ktor-client-gson:1.3.0
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5,org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5
//DEPS dev.zieger.utils:core:2.2.5,dev.zieger.utils:jdk:2.2.5

@file:Suppress("UNREACHABLE_CODE", "PropertyName")

import TestFlakyness.TestResult.*
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.console.ConsoleControl
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess


sealed class TestResult {
    data class Succeed(val completed: Int, val failed: Int, val skipped: Int,
                       val failedPercent: Float = failed / completed.toFloat() * 100f,
                       val skippedPercent: Float = skipped / (completed + skipped).toFloat() * 100f) : TestResult()

    data class Failed(val testClass: String,
                      val testMethod: String,
                      val exception: String,
                      val codeOrigin: String = "",
                      val exception2: String = "",
                      val codeOrigin2: String = "") : TestResult()
}

runBlocking {
    val runs = ArrayList<Succeed>()
    val failed = ArrayList<Failed>()
    var failedPrinted = 0
    var runCount = 0
    var failedRunCount = 0
    var failedTestCount = 0
    var lastMessage: String

    while (true) {
        progress {
            var runFailed = false
            val result = "./gradlew test".runCommand()?.run {
                (stdOutput?.reader()?.readText() ?: "") + (errOutput?.reader()?.readText() ?: "")
            } ?: ""
//            println(result)

            "(\\d+) tests completed, (\\d+) failed, (\\d+) skipped".toRegex().find(result)?.groupValues?.run {
                runs.add(Succeed(get(1).toInt(), get(2).toInt(), get(3).toInt()))
            }
            "(.*) > (.*) FAILED\\n *(.*) at (.*)\\n *Caused by: (.*) at (.*)".toRegex().findAll(result).forEach {
                it.groupValues.run {
                    val fail = Failed(get(1), get(2), get(3), get(4), get(5), get(6))
                    if (!failed.contains(fail)) failed.add(fail)
                    runFailed = true
                    failedTestCount++
                }
            }
            "Execution failed for task '(.*)'\\.\\n> A failure occurred while executing (.*)\\n *> (.*)".toRegex().find(result)?.groupValues?.run {
                val fail = Failed(get(1), get(2), get(3))
                if (!failed.contains(fail)) failed.add(fail)
                runFailed = true
            }
            runCount++
            if (runFailed) failedRunCount++
        }

        ConsoleControl.carriageReturn()
        repeat(100) { print(" ") }
        ConsoleControl.carriageReturn()

        if (failedPrinted < failed.size) {
            failed.subList(failedPrinted, failed.size).forEach {
                println("$it\n")
            }
            failedPrinted = failed.size
        }

        val failedTestsPercent = if (runs.isNotEmpty()) 100f * failedTestCount / (runs.avgNumCompleted * runCount) else 0f
        val failedRunsPercent = if (runCount > 0) 100f * failedRunCount / runCount else 0f
        lastMessage = "failed tests: ${String.format("%.2f", failedTestsPercent)}% - failed runs: ${String.format("%.2f", failedRunsPercent)}% ($runCount runs) "

        print(lastMessage)
    }
    exitProcess(0).asUnit()
}

val List<Succeed>.numCompleted get() = sumByLong { it.completed.toLong() }
val List<Succeed>.avgNumCompleted get() = if (isNotEmpty()) numCompleted / size else 0L

inline infix fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

suspend fun progress(block: suspend () -> Unit) {
    printProgress().let { block(); it() }
    repeat(3) { print("\b \b") }
}

fun printProgress(): () -> Unit {
    var idx = 0
    var printed = false
    val job = DefaultCoroutineScope().launchEx(interval = 75.milliseconds) {
        if (printed) repeat(3) { print("\b \b") }
        print("(${when (idx++) {
            0 -> "|"
            1 -> "/"
            2 -> "-"
            else -> {
                idx = 0
                "\\"
            }
        }
        })")
        printed = true
    }
    return { job.cancel() }
}
