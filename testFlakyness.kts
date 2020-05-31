#!/usr/bin/env kscript
//DEPS io.ktor:ktor-client-apache:1.3.0,io.ktor:ktor-client-gson:1.3.0
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5,org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5
//DEPS dev.zieger.utils:core:2.2.5,dev.zieger.utils:jdk:2.2.5

@file:Suppress("UNREACHABLE_CODE", "PropertyName")

import TestFlakyness.TestResult.*
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess


data class TestResult(val testsPerRun: Int = -1,
                      val runCount: Int = 0,
                      val failedRunCount: Int = 0,
                      val failedTestCount: Int = 0,
                      val failed: List<Failed> = emptyList()) {

    data class Failed(val runIdx: Int,
                      val testClass: String,
                      val testMethod: String,
                      val exception: String,
                      val codeOrigin: String = "",
                      val exception2: String = "",
                      val codeOrigin2: String = "")
}

val NUM_PARALLEL = 1

runBlocking {
    println("test flakyness with $NUM_PARALLEL parallel jobs\n")

    progress { extra ->
        (0 until NUM_PARALLEL).map {
            launchEx {
                var testResult = TestResult()
                testResult.run {
                    while (true) {
                        var runFailed = false
                        val result = "./gradlew test".runCommand()?.run {
                            (stdOutput?.reader()?.readText() ?: "") + (errOutput?.reader()?.readText() ?: "")
                        } ?: ""
                        print(result)

                        "(\\d+) tests completed, (\\d+) failed, (\\d+) skipped".toRegex().find(result)?.groupValues?.run {
                            testResult = copy(testsPerRun = get(1).toInt())
                        }
                        "(.*) > (.*) FAILED\\n *(.*) at (.*)\\n *Caused by: (.*) at (.*)".toRegex().findAll(result).forEach {
                            it.groupValues.run {
                                val fail = Failed(runCount, get(1), get(2), get(3), get(4), get(5), get(6))
                                testResult = copy(failed = failed + fail,
                                        failedTestCount = failedTestCount + 1)
                                runFailed = true
                            }
                        }
                        "Execution failed for task '(.*)'\\.\\n> A failure occurred while executing (.*)\\n *> (.*)".toRegex()
                                .findAll(result).forEach {
                                    it.groupValues.run {
                                        val fail = Failed(runCount, get(1), get(2), get(3))
                                        testResult = copy(failed = failed + fail, failedTestCount = failedTestCount + 1)
                                        runFailed = true
                                    }
                                }

                        testResult = copy(runCount = runCount + 1, failedRunCount = failedRunCount + if (runFailed) 1 else 0)
                        extra()
                    }
                }
            }
        }.joinAll()
    }
    exitProcess(0).asUnit()
}

suspend fun progress(block: suspend (TestResult.() -> Unit) -> Unit) {
    var extra: TestResult? = null
    printProgress { extra }.let { block { extra = this }; it() }
    repeat(3) { print("\b \b") }
}

suspend fun printProgress(extra: suspend () -> TestResult?): () -> Unit {
    var idx = 0
    var testResult = TestResult()
    var failedPrinted = 0
    var prevLastLine = ""

    val job = launchEx(interval = 75.milliseconds) {
        testResult = (extra() ?: testResult).apply {
            repeat(prevLastLine.length) { print("\b \b") }

            if (failed.size > failedPrinted) {
                val subList = failed.subList(failedPrinted, failed.size)
                println(subList.joinToString("\n"))
                failedPrinted = subList.size
            }

            val testFailedPercent = 0f
            val runFailedPercent = 0f
            val extraString = "failed tests: ${String.format("%.2f", testFailedPercent)}% - " +
                    "runs failed: ${String.format("%.2f", runFailedPercent)}% ($runCount runs) "
            prevLastLine = "$extraString(${when (idx++) {
                0 -> "|"
                1 -> "/"
                2 -> "-"
                else -> {
                    idx = 0
                    "\\"
                }
            }
            })"
            print(prevLastLine)
        }
    }
    return { job.cancel() }
}
