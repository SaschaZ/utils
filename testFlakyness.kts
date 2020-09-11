#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx5g")
@file:KotlinOpts("-J-server")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOnMaven("io.ktor:ktor-client-apache:1.3.0")
@file:DependsOnMaven("io.ktor:ktor-client-gson:1.3.0")
@file:DependsOnMaven("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
@file:DependsOnMaven("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
@file:DependsOnMaven("dev.zieger.utils:core:2.2.22")
@file:DependsOnMaven("dev.zieger.utils:jdk:2.2.22")
@file:DependsOnMaven("org.apache.commons:commons-lang3:3.10")
@file:DependsOnMaven("com.github.ajalt:mordant:1.2.1")

@file:Suppress("UNREACHABLE_CODE", "PropertyName", "LocalVariableName")


/**
 * To run this script you need to install kscript (https://github.com/holgerbrandl/kscript). You also need to make sure
 * that this file is executable. No parameters are needed for execution.
 *
 * This script will execute the unit tests of an existing gradle project repetitive to verify that tests are always
 * returning the same result.
 * To speed up the execution of the script multiple gradle jobs can be executed in parallel. Use the `-j` parameter to
 * define the number of parallel gradle jobs. For example: `./testFlakyness.kts -j 4` for using 4 parallel jobs. By
 * default only one job is used.
 */

import com.github.ajalt.mordant.TermColors
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.console.ConsoleControl
import dev.zieger.utils.log.console.LogColored
import dev.zieger.utils.log.console.termColored
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.onShutdown
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx
import dev.zieger.utils.time.base.TimeUnit.*
import dev.zieger.utils.time.milliseconds
import dev.zieger.utils.time.minus
import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.parse
import dev.zieger.utils.time.toDuration
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess

data class TestCase(
    val name: String,
    val clazz: String,
    val duration: IDurationEx
)

data class Failure(
    val name: String,
    val clazz: String,
    val duration: IDurationEx,
    val message: String,
    val exception: String,
    val stackTrace: String,
    val origin: String = ""
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

data class TestRunResult(val suites: List<TestSuiteResult>,
                         val executedTests: Int = suites.size,
                         val failedTests: Int = suites.sumBy { it.numFailures })


object ResultPrinter {

    const val FAIL_LOG_ROOT = "./failed/"

    private lateinit var activeSince: ITimeEx

    suspend fun progress(block: suspend (SendChannel<TestRunResult>) -> Unit) {
        activeSince = TimeEx()
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
        termColored {
            var idx = 0
            val progressJob = launchEx(interval = 100.milliseconds) {
                ConsoleControl.clearLine()
                printResult(channel)

                print(
                    "  ${red("(")}${
                        when (idx++) {
                            0 -> green("|")
                            1 -> green("/")
                            2 -> green("-")
                            else -> {
                                idx = 0
                                green("\\")
                            }
                        }
                    }${red(")")}  "
                )
            }
            return { progressJob.cancel() }
        }
        return {}
    }

    private var progressIdx = 0
    private val TermColors.progress
        get() = "  ${red("(")}${
            when (progressIdx++) {
                0 -> green("|")
                1 -> green("/")
                2 -> green("-")
                else -> {
                    progressIdx = 0
                    green("\\")
                }
            }
        }${red(")")}  "

    private var executedRuns = 0
    private var runsWithFailedTests = 0
    private var executedTests = 0
    private var failedTests = 0
    private var lastResultOutput = ""

    private fun printResult(channel: ReceiveChannel<TestRunResult>) {
        termColored {
            channel.getAllAvailable().forEach { testRun ->
                executedRuns++
                if (testRun.failedTests > 0) runsWithFailedTests++
                executedTests += testRun.executedTests
                failedTests += testRun.failedTests

                val failedRunsPercent = (100f * runsWithFailedTests) / executedRuns
                val failedTestsPercent = (100f * failedTests) / executedTests

                printFailed(testRun)
                lastResultOutput = cyan(
                    "failed tests: ${
                        if (failedTests > 0) red("${"%.2f%%".format(failedTestsPercent)} ($failedTests/$executedTests)")
                        else green("${"%.2f%%".format(failedTestsPercent)} ($failedTests/$executedTests)")
                    } - failed runs: ${
                        if (runsWithFailedTests > 0) red("${"%.2f%%".format(failedRunsPercent)} ($runsWithFailedTests/$executedRuns)")
                        else green("${"%.2f%%".format(failedRunsPercent)} ($runsWithFailedTests/$executedRuns)")
                    }"
                )
            }
            val duration = (TimeEx() - activeSince).formatDuration(YEAR, DAY, HOUR, MINUTE, SECOND)
            if (lastResultOutput.isBlank()) print(cyan("No runs have finished yet. [$duration]"))
            else print("$lastResultOutput ${cyan("[$duration]")}")
        }
    }

    private fun printFailed(testRun: TestRunResult) {
        val loggedSuites = ArrayList<TestSuiteResult>()
        termColored {
            testRun.suites.map { suite -> suite to suite.failure }.forEach { (suite, failures) ->
                if (suite !in loggedSuites && failures.isNotEmpty()) {
                    File(FAIL_LOG_ROOT).mkdirs()
                    File(
                        FAIL_LOG_ROOT
                                + TimeEx().formatTime(DateFormat.CUSTOM("yyyy-MM-dd-HH-mm-ss-SSS"))
                                + ".log"
                    ).writeText("$suite\n\n")
                    loggedSuites += suite
                }

                failures.forEach {
                    Log.e("${it.clazz.split(".").last()}.${it.name}(${it.origin})\n\t${it.message}")
                }
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
        if (folder.exists()) folder.deleteRecursively()
        folder.mkdirs()
        File(".").copyRecursively(folder, overwrite = true)
        "chmod +x gradlew".runCommand(folder)
        deleteTemporaryFiles(folder)
        val ctx = coroutineContext
        onShutdown {
            ctx.cancel()
            folder.deleteRecursively()
        }
        return folder
    }

    private suspend fun deleteTemporaryFiles(folder: File) {
        "find . -iregex ^.*/build\$".runCommand(folder)?.run { stdOutput?.reader()?.readText() }?.split("./")
            ?.forEach { File(it).deleteRecursively() }
    }

    private suspend fun runTests(folder: File): TestRunResult {
        "./gradlew test".runCommand(folder)
        return ResultParser.parseResults(folder)
    }
}


object ResultParser {

    suspend fun parseResults(projectRoot: File): TestRunResult =
        TestRunResult(("""find . -iregex ^.*/test[DebugUnitTest]*/TEST\-[a-zA-Z0-9\.\-\_]+\.xml$"""
            .runCommand(projectRoot)?.run { (stdOutput?.reader()?.readText() ?: "") } ?: "")
            .split("\n").mapNotNull {
                if (it.isBlank()) null
                else parseResult(File(projectRoot.absolutePath + "/" + it.trim().removePrefix(".")))
            })

    private fun parseResult(file: File): TestSuiteResult? {
        val content = file.readText()

        val testSuiteRegex =
            """<testsuite name="([a-zA-Z0-9.\-_]+)" tests="(\d+)" skipped="(\d+)" failures="(\d+)" errors="(\d+)" timestamp="([0-9\-:T]+)" hostname="([a-zA-Z0-9]+)" time="([0-9.]+)">\n\W+<properties/>""".toRegex()
        var testSuiteResult = testSuiteRegex.find(content)?.groupValues?.run {
            TestSuiteResult(
                packageName = get(1),
                numTests = get(2).toInt(),
                numSkipped = get(3).toInt(),
                numFailures = get(4).toInt(),
                numErrors = get(5).toInt(),
                time = get(6).parse(),
                issuer = get(7),
                duration = get(8).toDouble().toDuration(SECOND)
            )
        }

        val testCaseRegex =
            """<testcase name="([a-zA-Z0-9()\-_]+)" classname="([a-zA-Z0-9.]+)" time="([0-9.]+)"/?>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(testCases = testCaseRegex.findAll(content).mapNotNull {
            it.groupValues.run {
                TestCase(
                    name = get(1),
                    clazz = get(2),
                    duration = get(3).toDouble().toDuration(SECOND)
                )
            }
        }.toList())

        val stdOutRegex = """<system-out><!\[CDATA\[([\D\d]*)]]></system-out>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(stdOut = stdOutRegex.find(content)?.groupValues?.get(1) ?: "")

        val errOutRegex = """<system-out><!\[CDATA\[([\D\d]*)]]></system-out>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(errOut = errOutRegex.find(content)?.groupValues?.get(1) ?: "")

        val failureRegex =
            """<testcase name="([a-zA-Z0-9()]+)" classname="([a-zA-Z0-9.]+)" time="([0-9.]+)">\n\W+<failure message="(.*)" type="(.+)">([^<>]*)</failure>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(failure = failureRegex.findAll(content).mapNotNull {
            it.groupValues.run {
                Failure(
                    name = unescapeHtml4(get(1)),
                    clazz = unescapeHtml4(get(2)),
                    duration = unescapeHtml4(get(3)).toDouble().toDuration(SECOND),
                    message = unescapeHtml4(get(4)),
                    exception = unescapeHtml4(get(5)),
                    stackTrace = unescapeHtml4(get(6))
                )
            }
        }.toList())

        val failedOrigins = testSuiteResult?.failure?.map { fail ->
            "${fail.clazz}${'\\'}${'$'}${fail.name.removeSuffix("()")}${'\\'}${'$'}[0-9a-zA-Z.]+\\(([a-zA-Z0-9.:]+)\\)".toRegex()
                .findAll(content).firstOrNull()?.groupValues?.getOrNull(1)
        }

        return testSuiteResult?.copy(failure = testSuiteResult.failure.mapIndexed { idx, value ->
            value.copy(origin = failedOrigins?.getOrNull(idx) ?: "")
        })
    }
}


runBlocking {
    val NUM_PARALLEL = 1
    LogColored.initialize()

    val jobs = if (args.getOrNull(0) == "-j") args.getOrNull(1)?.toInt()
        ?: { throw IllegalArgumentException("invalid arguments: ${args.joinToString()}") }.invoke()
    else NUM_PARALLEL

    ConsoleControl.clearScreen()
    termColored {
        Log.v(brightBlue("test flakyness with ${(bold + cyan)("$jobs")} ${if (jobs > 1) "parallel jobs" else "job"}\n"))
    }

    ResultPrinter.progress { channel -> (0 until jobs).map { channel.launchJob(it) }.joinAll() }
    exitProcess(0).asUnit()
}
