#!/usr/bin/env kscript
//DEPS io.ktor:ktor-client-apache:1.3.0,io.ktor:ktor-client-gson:1.3.0
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6,org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.6
//DEPS dev.zieger.utils:core:2.2.14,dev.zieger.utils:jdk:2.2.14
//DEPS org.apache.commons:commons-lang3:3.10
// DO NOT REMOVE COMMENTS ABOVE. THEY ARE NEEDED FOR kscript.
@file:Suppress("UNREACHABLE_CODE", "PropertyName", "LocalVariableName")


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

import TestFlakyness.CpuLoadProvider.cpuLoad
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.coroutines.scope.ICoroutineScopeEx
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.toDuration
import dev.zieger.utils.time.parse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.system.exitProcess


object Utils {

    private const val THREAD_POOL_SIZE = 4

    val threadPool: ExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    suspend inline fun <T> executeNativeBlocking(crossinline block: () -> T): T = suspendCancellableCoroutine { cont ->
        threadPool.execute {
            try {
                cont.resume(block())
            } catch (e: Throwable) {
                if (!cont.isCompleted) cont.resumeWithException(e)
            }
        }
    }
}


data class TestCase(
    val name: String,
    val className: String,
    val duration: IDurationEx
)

data class TestSuiteResult(
    val packageName: String,
    val numTests: Int,
    val numSkipped: Int,
    val numFailures: Int,
    val numErrors: Int,
    val time: ITimeEx,
    val issuer: String,
    val duration: IDurationEx,
    val testCases: List<TestCase> = emptyList(),
    val stdOut: String = "",
    val errOut: String = "",
    val failure: List<Failure> = emptyList()
)

data class Failure(val exception: String,
                   val origin: String,
                   val stackTrace: String)

data class TestRunResult(val suites: List<TestSuiteResult>,
                         val executedTests: Int = suites.size,
                         val failedTests: Int = suites.sumBy { it.numFailures })


object ResultPrinter {

    suspend fun progress(block: suspend (SendChannel<TestRunResult>) -> Unit) {
        val channel = Channel<TestRunResult>()
        printProgress(channel as ReceiveChannel<TestRunResult>).let { block(channel as SendChannel<TestRunResult>); it() }
        repeat(3) { print("\b \b") }
    }

    private fun <T : Any> ReceiveChannel<T>.getAllAvailable(): List<T> {
        var result: T? = poll()
        val results = ArrayList<T?>()
        while (result != null) {
            results.add(result)
            result = poll()
        }
        return results.mapNotNull { it }
    }


    private fun printProgress(channel: ReceiveChannel<TestRunResult>): () -> Unit {
        var idx = 0
        var lastOutputLength = 0
        val progressJob = launchEx(interval = 75.milliseconds) {
            if (lastOutputLength > 0) repeat(lastOutputLength) { print("\b \b") }

            channel.getAllAvailable().forEach { printResult(it) }

            val output = "load: ${"%6.2f%%".format(cpuLoad)} " +
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

            print(output)
            lastOutputLength = output.length
        }

        return { progressJob.cancel() }
    }

    private var executedRuns = 0
    private var runsWithFailedTests = 0
    private var executedTests = 0
    private var failedTests = 0

    private fun printResult(results: TestRunResult) {
        executedRuns++
        if (results.failedTests > 0) runsWithFailedTests++
        executedTests += results.executedTests
        failedTests += results.failedTests

        val failedRunsPercent = (100f * runsWithFailedTests) / executedRuns
        val failedTestsPercent = (100f * failedTests) / executedTests

        printFailed(results)
        repeat(100) { print("\b \b") }
        print(
            "failed tests: ${"%.2f%%".format(failedTestsPercent)} ($failedTests/$executedTests) - " +
                    "failed runs: ${"%.2f%%".format(failedRunsPercent)} ($runsWithFailedTests/$executedRuns) "
        )
    }

    private fun printFailed(results: TestRunResult) {
        results.suites.flatMap { it.failure }.forEach { println("\n$it") }
    }
}


object TestRunner {

    suspend fun SendChannel<TestRunResult>.launchJob(id: Int): Job = launchEx {
        val folder = copyProject(id)
        while (true) send(runTests(folder))
    }

    private suspend fun copyProject(id: Int): File {
        val folder = File("/tmp/flakynessTest$id")
        folder.mkdirs()
        File(".").copyRecursively(folder, overwrite = true)
        "chmod +x gradlew".runCommand(folder)
        deleteTemporaryFiles(folder)
        val ctx = coroutineContext
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                ctx.cancel()
                folder.deleteRecursively()
            }
        })
        return folder
    }

    private suspend fun deleteTemporaryFiles(folder: File) {
        "find . -iregex ^.*/build\$".runCommand(folder)?.run { stdOutput?.reader()?.readText() }?.split("./")
            ?.forEach { File(it).deleteRecursively() }
    }

    private suspend fun runTests(folder: File): TestRunResult {
        deleteTemporaryFiles(folder)
        "./gradlew test".runCommand(folder)
        return ResultParser.parseResults(folder)
    }
}


object ResultParser {

    suspend fun parseResults(projectRoot: File): TestRunResult =
        TestRunResult(("""find /tmp -iregex ^.*/test[ReleaseUnitTest]*/TEST\-[a-zA-Z0-9\.\-\_]+\.xml$"""
            .runCommand(projectRoot)?.run { (stdOutput?.reader()?.readText() ?: "") } ?: "")
            .split("/tmp").mapNotNull { if (it.isBlank()) null else parseResult(File("/tmp${it.trim()}")) })

    private fun parseResult(file: File): TestSuiteResult? {
        val content = file.readText()

        val testSuiteRegex =
            """<testsuite name="([a-zA-Z0-9.\-_]+)" tests="(\d+)" skipped="(\d+)" failures="(\d+)" errors="(\d+)" timestamp="([0-9\-:T]+)" hostname="(\D+)" time="([0-9.]+)">\n\W+<properties/>""".toRegex()
        var testSuiteResult = testSuiteRegex.find(content)?.groupValues?.run {
            TestSuiteResult(
                get(1), get(2).toInt(), get(3).toInt(), get(4).toInt(), get(5).toInt(), get(6).parse(), get(7),
                get(8).toDouble().toDuration(TimeUnit.SECOND)
            )
        }

        val testCaseRegex =
            """<testcase name="([a-zA-Z0-9()\-_]+)" classname="([a-zA-Z0-9.]+)" time="([0-9.]+)"/>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(testCases = testCaseRegex.findAll(content).mapNotNull {
            it.groupValues.run {
                TestCase(get(1), get(2), get(3).toDouble().toDuration(TimeUnit.SECOND))
            }
        }.toList())

        val stdOutRegex = """<system-out><!\[CDATA\[([\D\d]*)]]></system-out>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(stdOut = stdOutRegex.find(content)?.groupValues?.get(1) ?: "")

        val errOutRegex = """<system-out><!\[CDATA\[([\D\d]*)]]></system-out>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(errOut = errOutRegex.find(content)?.groupValues?.get(1) ?: "")

        val failureRegex =
            """<testcase name="([a-zA-Z0-9]+)" classname="([a-zA-Z0-9.]+)" time="([0-9.]+)">\n\W+<failure message="(.*)" type="(.+)">([^<>]*)</failure>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(failure = failureRegex.findAll(content).mapNotNull {
            it.groupValues.run {
                Failure(unescapeHtml4(get(1)), unescapeHtml4(get(2)), unescapeHtml4(get(3)))
            }
        }.toList())

        return testSuiteResult
    }
}


object CpuLoadProvider : ICoroutineScopeEx by DefaultCoroutineScope() {

    private const val CPU_READ_INTERVAL = 500L
    private const val CPU_LOAD_FIFO_SIZE = 3

    fun init() {
        launchEx { readCpuLoad() }
    }

    private val cpuLoadFiFo = FiFo<Double>(CPU_LOAD_FIFO_SIZE)

    private suspend fun readCpuLoad() {
        while (true) {
            val nanoBefore = System.nanoTime()
            val cpuBefore = cpuT

            delay(CPU_READ_INTERVAL)

            val cpuAfter = cpuT
            val nanoAfter = System.nanoTime()

            val percent = if (nanoAfter > nanoBefore)
                ((cpuAfter - cpuBefore) * 1000000.0) / (nanoAfter - nanoBefore)
            else 0.0

            cpuLoadFiFo.put(percent * 100)
        }
    }

    private val cpuT: Long
        get() {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine()
            val pattern = """cpu +(\d+) +(\d+) +(\d+) +(\d+)""".toRegex()
            val groups = pattern.find(line)?.groupValues

            var cpuUser = 0L
            var cpuSystem = 0L
            if (groups?.isNotEmpty() == true) {
                cpuUser = groups[1].toLong()
                cpuSystem = groups[3].toLong()
            } else println("COULD NOT ACCESS CPU DATA")
            return cpuUser + cpuSystem
        }

    val cpuLoad: Double get() = ArrayList(cpuLoadFiFo).average()
}


runBlocking {
    val NUM_PARALLEL = 6

    val jobs = if (args.getOrNull(0) == "-j")
        args.getOrNull(1)?.toInt() ?: throw IllegalArgumentException("invalid arguments: ${args.joinToString()}")
    else NUM_PARALLEL

    println("test flakyness with $jobs parallel jobs\n")

    CpuLoadProvider.init()
    ResultPrinter.progress { channel -> (0 until jobs).map { channel.launchJob(it) }.joinAll() }
    exitProcess(0).asUnit()
}
