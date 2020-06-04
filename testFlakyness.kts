#!/usr/bin/env kscript
//DEPS io.ktor:ktor-client-apache:1.3.0,io.ktor:ktor-client-gson:1.3.0
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5,org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5
//DEPS dev.zieger.utils:core:2.2.5,dev.zieger.utils:jdk:2.2.5
// DO NOT REMOVE COMMENTS ABOVE. THESE ARE NEEDED FOR kscript.
@file:Suppress("UNREACHABLE_CODE", "PropertyName")

/**
 * To run this script you need to install kscript (https://github.com/holgerbrandl/kscript). You also need to make sure
 * that this file is executable. No parameters are needed for execution.
 *
 * This script will execute the unit tests of an existing gradle project repetitive to verify that tests are always
 * returning the same result.
 * To speed up the execution of the script [NUM_PARALLEL] gradle jobs are executed in parallel. Because gradle does not
 * allow multiple executions at the same time in the same directory the script will create [NUM_PARALLEL] copies of the
 * project to test.
 */

import TestFlakyness.TestResult.*
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess


data class TestResult(
    val id: Int,
    var testsPerRun: Int = -1,
    var runCount: Int = 0,
    var failedRunCount: Int = 0,
    var failedTestCount: Int = 0,
    var failed: List<Failed> = emptyList()
) {

    data class Failed(
        var runIdx: Int,
        var testClass: String,
        var testMethod: String,
        var exception: String,
        var codeOrigin: String = "",
        var exception2: String = "",
        var codeOrigin2: String = ""
    )
}

suspend fun progress(block: suspend (SendChannel<TestResult>) -> Unit) {
    val channel = Channel<TestResult>()
    printProgress(channel as ReceiveChannel<TestResult>).let { block(channel as SendChannel<TestResult>); it() }
    repeat(3) { print("\b \b") }
}

suspend fun printProgress(channel: ReceiveChannel<TestResult>): () -> Unit {
    val results = ArrayList<TestResult>()
    val channelJob = launchEx {
        for (result in channel) results += result
    }

    var idx = 0
    var progressPrinted = false
    val progressJob = launchEx(interval = 75.milliseconds) {
        if (progressPrinted) repeat(3) { print("\b \b") }

        printResult(ArrayList(results))

        print(
            "(${when (idx++) {
                0 -> "|"
                1 -> "/"
                2 -> "-"
                else -> {
                    idx = 0
                    "\\"
                }
            }
            })"
        )
        progressPrinted = true
    }

    return {
        channelJob.cancel()
        progressJob.cancel()
    }
}

suspend fun SendChannel<TestResult>.launchJob(id: Int): Job = launchEx {
    val folder = copyProject(id)
    TestResult(id).run {
        while (true) {
            runTests(folder)
            send(this)
        }
    }
}

suspend fun copyProject(id: Int): File {
    val target = File("/tmp/flakynessTest$id")
    target.mkdirs()
    File(".").copyRecursively(target, overwrite = true)
    val ctx = coroutineContext
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            ctx.cancel()
            target.deleteRecursively()
        }
    })
    return target
}

suspend fun TestResult.runTests(folder: File) {
    var runFailed = false
    "chmod +x gradlew".runCommand(folder)
    val result = "./gradlew test".runCommand(folder)?.run {
        (stdOutput?.reader()?.readText() ?: "") + (errOutput?.reader()?.readText() ?: "")
    } ?: ""
//    println(result)

    "(\\d+) tests completed, (\\d+) failed, (\\d+) skipped".toRegex().find(result)?.groupValues?.run {
        testsPerRun = get(1).toInt()
    }
    "(.*) > (.*) FAILED\\n *(.*) at (.*)\\n *Caused by: (.*) at (.*)".toRegex().findAll(result).forEach {
        it.groupValues.run {
            val fail = Failed(runCount, get(1), get(2), get(3), get(4), get(5), get(6))
            failed = failed + fail
            failedTestCount += 1
            runFailed = true
        }
    }
    "Execution failed for task '(.*)'\\.\\n> A failure occurred while executing (.*)\\n *> (.*)".toRegex()
        .findAll(result).forEach {
            it.groupValues.run {
                val fail = Failed(runCount, get(1), get(2), get(3))
                failed = failed + fail
                failedTestCount += 1
                runFailed = true
            }
        }

    runCount += 1
    failedRunCount += if (runFailed) 1 else 0
}

val printedExceptions = ArrayList<Failed>()

fun printExceptions(results: List<TestResult>) {
    val exceptions = results.flatMap { it.failed }.filter { !printedExceptions.contains(it) }.distinct()
    if (exceptions.isNotEmpty()) {
        repeat(100) { print("\b \b") }
        println(exceptions.joinToString("\n"))
        printedExceptions.addAll(exceptions)
    }
}

var prevLog = ""

fun printResult(results: List<TestResult>) {
    printExceptions(results)

    val testsPerRun = results.firstOrNull { it.runCount > 0 }?.runCount ?: 0
    val runsPerId = results.groupBy { it.id }
    val executedRuns = runsPerId.values.map { it.maxBy { tr -> tr.runCount }?.runCount ?: 0 }.sum()
    val executedTests = executedRuns * testsPerRun
    val failedRuns = runsPerId.values.map { it.maxBy { tr -> tr.failedRunCount }?.failedRunCount ?: 0 }.sum()
    val failedTests = runsPerId.values.map { it.maxBy { tr -> tr.failedTestCount }?.failedTestCount ?: 0 }.sum()

    val failedRunsPercent = if (executedRuns > 0) 100f * failedRuns / executedRuns else 0f
    val failedTestsPercent = if (executedTests > 0) 100f * failedTests / executedTests else 0f

    val newLog = "failed tests: ${String.format("%.2f%%", failedTestsPercent)} - " +
            "failed runs: ${String.format("%.2f%%", failedRunsPercent)} - " +
            "runs: $executedRuns "

    if (prevLog != newLog) {
        repeat(100) { print("\b \b") }
        print(newLog)
        prevLog = newLog
    }
}

val NUM_PARALLEL = 6

runBlocking {
    println("test flakyness with $NUM_PARALLEL parallel jobs\n")

    progress { channel -> (0 until NUM_PARALLEL).map { channel.launchJob(it) }.joinAll() }
    exitProcess(0).asUnit()
}