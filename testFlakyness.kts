#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx5g")
@file:KotlinOpts("-J-server")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOnMaven("io.ktor:ktor-client-apache:1.3.0")
@file:DependsOnMaven("io.ktor:ktor-client-gson:1.3.0")
@file:DependsOnMaven("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
@file:DependsOnMaven("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.6")
@file:DependsOnMaven("dev.zieger.utils:core:2.2.14")
@file:DependsOnMaven("dev.zieger.utils:jdk:2.2.14")
@file:DependsOnMaven("org.apache.commons:commons-lang3:3.10")
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

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.log.console.ConsoleControl
import dev.zieger.utils.log.console.LogColored
import dev.zieger.utils.log.console.LogColored.scope
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
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess

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
        printProgress(channel as ReceiveChannel<TestRunResult>).let {
            block(channel as SendChannel<TestRunResult>)
            it()
        }
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
        scope {
            var idx = 0
            var lastOutputLength = 0
            val progressJob = launchEx(interval = 500.milliseconds) {
                if (lastOutputLength > 0) repeat(lastOutputLength) { print("\b \b") }

                printResult(channel)

                val progress = "  ${red("(")}${when (idx++) {
                    0 -> green("|")
                    1 -> green("/")
                    2 -> green("-")
                    else -> {
                        idx = 0
                        green("\\")
                    }
                }
                }${red(")")}  "

                print(progress)
                lastOutputLength = progress.length
            }
            return { progressJob.cancel() }
        }
        return {}
    }

    private var executedRuns = 0
    private var runsWithFailedTests = 0
    private var executedTests = 0
    private var failedTests = 0
    private val printedFailures = HashSet<Failure>()
    private var printed = false
    private var lastResultOutput = ""

    private fun printResult(channel: ReceiveChannel<TestRunResult>) {
        scope {
            channel.getAllAvailable().forEach { testRun ->
                printed = true
                executedRuns++
                if (testRun.failedTests > 0) runsWithFailedTests++
                executedTests += testRun.executedTests
                failedTests += testRun.failedTests

                val failedRunsPercent = (100f * runsWithFailedTests) / executedRuns
                val failedTestsPercent = (100f * failedTests) / executedTests

                printFailed(testRun)
                lastResultOutput = green(
                    "failed tests: ${"%.2f%%".format(failedTestsPercent)} ($failedTests/$executedTests) - " +
                            "failed runs: ${"%.2f%%".format(failedRunsPercent)} ($runsWithFailedTests/$executedRuns) "
                )
            }
            ConsoleControl.clearLine()
            repeat(1000) { print("\b \b") }
            if (lastResultOutput.isBlank()) print(cyan("No runs have finished yet."))
            else print(lastResultOutput)
        }
    }

    private fun printFailed(testRun: TestRunResult) {
        testRun.suites.flatMap { it.failure }.forEach {
            if (!printedFailures.contains(it)) {
                println(it)
                printedFailures.add(it)
            }
        }
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
                Failure(unescapeHtml4(get(1)), unescapeHtml4(get(2)), unescapeHtml4(get(6)))
            }
        }.toList())

        return testSuiteResult
    }
}


runBlocking {
    val NUM_PARALLEL = 6
    LogColored.initialize()

    val jobs = if (args.getOrNull(0) == "-j")
        args.getOrNull(1)?.toInt() ?: throw IllegalArgumentException("invalid arguments: ${args.joinToString()}")
    else NUM_PARALLEL

    scope {
        println(brightBlue("test flakyness with ${(bold + cyan)("$jobs")} parallel jobs\n"))
    }

    ResultPrinter.progress { channel -> (0 until jobs).map { channel.launchJob(it) }.joinAll() }
    exitProcess(0).asUnit()
}
