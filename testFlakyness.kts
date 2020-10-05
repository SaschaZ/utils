#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx5g")
@file:KotlinOpts("-J-server")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOnMaven("io.ktor:ktor-client-apache:1.3.0")
@file:DependsOnMaven("io.ktor:ktor-client-gson:1.3.0")
@file:DependsOnMaven("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
@file:DependsOnMaven("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
@file:DependsOnMaven("dev.zieger.utils:core:2.3.0")
@file:DependsOnMaven("dev.zieger.utils:jdk:2.3.0")
@file:DependsOnMaven("com.googlecode.lanterna:lanterna:3.1.0-beta2")
@file:DependsOnMaven("org.apache.commons:commons-lang3:3.10")
@file:DependsOnMaven("com.github.ajalt:mordant:1.2.1")

@file:Suppress("UNREACHABLE_CODE", "PropertyName", "LocalVariableName")


/**
 * To run this script you need to install kscript (https://github.com/holgerbrandl/kscript). You also need to make sure
 * that this file is executable. No parameters are needed for execution.
 *
 * Because of a bug in KScript it is needed, that the Utils lib is published to the local maven repository.
 *
 * This script will execute the unit tests of an existing gradle project repetitive to verify that tests are always
 * returning the same result.
 * To speed up the execution of the script multiple gradle jobs can be executed in parallel. Use the `-j` parameter to
 * define the number of parallel gradle jobs. For example: `./testFlakyness.kts -j 4` for using 4 parallel jobs. By
 * default only one job is used.
 */

import TestFlakyness.TestRunner.launchJob
import com.github.ajalt.mordant.TermColors
import com.googlecode.lanterna.TextColor.ANSI.*
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.gui.console.LanternaConsole
import dev.zieger.utils.gui.console.IScope
import dev.zieger.utils.gui.console.scope
import dev.zieger.utils.gui.console.invoke
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.console.ConsoleControl
import dev.zieger.utils.log.console.LogColored
import dev.zieger.utils.log.console.termColored
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.onShutdown
import dev.zieger.utils.time.*
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx
import dev.zieger.utils.time.base.TimeUnit.*
import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.parse
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4
import java.awt.Color.CYAN
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess


data class TestRunResult(val suites: List<TestSuiteResult>,
                         val executedTests: Int = suites.size,
                         val failedTests: Int = suites.sumBy { it.numFailures },
                         val retriedTests: Int = suites.sumBy { it.retries.size })

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
        val retries: List<Retry> = emptyList(),
        val failure: List<Failure> = emptyList()
)

data class TestCase(
        val name: String,
        val clazz: String,
        val duration: IDurationEx
)

data class Failure(val name: String,
                   val clazz: String,
                   val duration: IDurationEx,
                   val message: String,
                   val exception: String,
                   val stackTrace: String,
                   val origin: String = "")

data class Retry(val clazz: String,
                 val message: String,
                 val cause: String,
                 val stackTrace: String)


class ResultPrinter(scope: LanternaConsole.Scope) : IScope by scope {

    private const val FAIL_LOG_ROOT = "./failed/"
    private const val RETRY_LOG_ROOT = "./retries/"

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
            val progressJob = DefaultCoroutineScope().launchEx(interval = 100.milliseconds) {
                ConsoleControl.clearLine()
                printResult(channel)
                print(progress)
            }
            return { progressJob.cancel() }
        }
        return {}
    }

    private var progressIdx = 0
    private val TermColors.progress
        get() = "${red(sideProgress(0))}${
            when (progressIdx++) {
                0 -> green("|")
                1 -> green("/")
                2 -> green("-")
                else -> {
                    progressIdx = 0
                    green("\\")
                }
            }
        }${red(sideProgress(3, ')'))}"

    private fun sideProgress(idx: Int, char: Char = '(') = when (idx) {
        0 -> "   $char"
        1 -> "  $char "
        2 -> " $char  "
        else -> "$char   "
    }

    private var executedRuns = 0
    private var runsWithFailedTests = 0
    private var runsWithRetries = 0

    private var executedTests = 0
    private var failedTests = 0
    private var retriedTests = 0
    private var lastResultOutput = ""

    private fun printResult(channel: ReceiveChannel<TestRunResult>) {
        termColored {
            channel.getAllAvailable().forEach { testRun ->
                executedRuns++
                if (testRun.failedTests > 0) runsWithFailedTests++
                if (testRun.retriedTests > 0) runsWithRetries++
                executedTests += testRun.executedTests
                retriedTests += testRun.retriedTests
                failedTests += testRun.failedTests

                val failedRunsPercent = (100f * runsWithFailedTests) / executedRuns
                val retriedRunsPercent = (100f * runsWithRetries) / executedRuns
                val failedTestsPercent = (100f * failedTests) / executedTests
                val retriedTestsPercent = (100f * retriedTests) / executedTests

                printRetry(testRun)
                printFailed(testRun)
                val testsResult = "${"%.2f%%".format(retriedTestsPercent)}/${"%.2f%%".format(failedTestsPercent)} ($retriedTests/$failedTests)"
                val runsResult = "${"%.2f%%".format(retriedRunsPercent)}/${"%.2f%%".format(failedRunsPercent)} ($runsWithRetries/$runsWithFailedTests)"
                lastResultOutput = cyan(
                        "$executedTests tests: ${
                            when {
                                failedTests > 0 -> red(testsResult)
                                retriedTests > 0 -> yellow(testsResult)
                                else -> green(testsResult)
                            }
                        } - $executedRuns runs: ${
                            when {
                                failedTests > 0 -> red(runsResult)
                                retriedTests > 0 -> yellow(runsResult)
                                else -> green(runsResult)
                            }
                        }"
                )
            }
            val duration = TimeEx() - activeSince
            if (lastResultOutput.isBlank()) print(cyan("No runs have finished yet. [${duration.formatDuration(YEAR, DAY, HOUR, MINUTE, SECOND)}]"))
            else print("$lastResultOutput - " +
                    "${(duration / executedRuns.toFloat()).formatDuration(YEAR, DAY, HOUR, MINUTE, SECOND)} per run - " +
                    cyan("[${duration.formatDuration(YEAR, DAY, HOUR, MINUTE, SECOND)}]"))
        }
    }


    private fun printRetry(testRun: TestRunResult) {
        val loggedSuites = ArrayList<TestSuiteResult>()
        termColored {
            testRun.suites.map { suite -> suite to suite.retries }.forEach { (suite, retry) ->
                if (suite !in loggedSuites && retry.isNotEmpty()) {
                    File(RETRY_LOG_ROOT).mkdirs()
                    File(RETRY_LOG_ROOT
                            + TimeEx().let { t -> t.formatTime(DateFormat.FILENAME/*CUSTOM("yyyy-MM-dd-HH-mm-ss-SSS")*/) + "-${t.millis % 1000}" }
                            + ".log").writeText("$suite\n\n")
                    loggedSuites += suite
                }

                retry.forEach {
                    println(yellow("[${TimeEx()}] ${it.clazz.split(".").last()}(${it.message})"))
                }
            }
        }
    }

    private fun printFailed(testRun: TestRunResult) {
        val loggedSuites = ArrayList<TestSuiteResult>()
        termColored {
            testRun.suites.map { suite -> suite to suite.failure }.forEach { (suite, failures) ->
                if (suite !in loggedSuites && failures.isNotEmpty()) {
                    File(FAIL_LOG_ROOT).mkdirs()
                    File(FAIL_LOG_ROOT
                            + TimeEx().let { t -> t.formatTime(DateFormat.FILENAME/*CUSTOM("yyyy-MM-dd-HH-mm-ss-SSS")*/) + "-${t.millis % 1000}" }
                            + ".log").writeText("$suite\n\n")
                    loggedSuites += suite
                }

                failures.forEach {
                    println(red("[${TimeEx()}] ${it.clazz.split(".").last()}.${it.name}(${it.origin})\n\t${it.message}"))
                }
            }
        }
    }
}


object TestRunner {

    private val scope: CoroutineScope = IoCoroutineScope()

    suspend fun SendChannel<TestRunResult>.launchJob(id: Int): Job = scope.launchEx {
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
        "./gradlew lib:testDebug".runCommand(folder)
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
                TestCase(name = get(1),
                        clazz = get(2),
                        duration = get(3).toDouble().toDuration(SECOND))
            }
        }.toList())

        val stdOutRegex = """<system-out><!\[CDATA\[([\D\d]*)]]></system-out>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(stdOut = stdOutRegex.find(content)?.groupValues?.get(1) ?: "")

        val errOutRegex = """<system-err><!\[CDATA\[([\D\d]*)]]><\/system-err>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(errOut = errOutRegex.find(content)?.groupValues?.get(1) ?: "")

        val failureRegex =
                """<testcase name="([a-zA-Z0-9()]+)" classname="([a-zA-Z0-9.]+)" time="([0-9.]+)">\n\W+<failure message="(.*)" type="(.+)">([^<>]*)</failure>""".toRegex()
        testSuiteResult = testSuiteResult?.copy(failure = failureRegex.findAll(content).mapNotNull {
            it.groupValues.run {
                Failure(name = unescapeHtml4(get(1)),
                        clazz = unescapeHtml4(get(2)),
                        duration = unescapeHtml4(get(3)).toDouble().toDuration(SECOND),
                        message = unescapeHtml4(get(4)),
                        exception = unescapeHtml4(get(5)),
                        stackTrace = unescapeHtml4(get(6)))
            }
        }.toList())

        val failedOrigins = testSuiteResult?.failure?.map { fail ->
            "${fail.clazz}${'\\'}${'$'}${fail.name.removeSuffix("()")}${'\\'}${'$'}[0-9a-zA-Z.]+\\(([a-zA-Z0-9.:]+)\\)".toRegex()
                    .findAll(content).firstOrNull()?.groupValues?.getOrNull(1)
        }
        testSuiteResult = testSuiteResult?.copy(failure = testSuiteResult.failure.mapIndexed { idx, value ->
            value.copy(origin = failedOrigins?.getOrNull(idx) ?: "")
        })

        val retryRegex = """#[0-9]+: ([\w :<>]+)\n(Cause: ([\w :<>]+))?([\w\n ().${'$'}:?]+)""".toRegex()
        testSuiteResult = testSuiteResult?.copy(
                retries = retryRegex.findAll(testSuiteResult.stdOut + "\n" + testSuiteResult.errOut).mapNotNull {
                    it.groupValues.run {
                        Retry(clazz = testSuiteResult?.packageName ?: "UNKNOWN",
                                message = get(1),
                                cause = get(3),
                                stackTrace = get(4))
                    }
                }.toList())

        return testSuiteResult
    }
}


runBlocking {
    val NUM_PARALLEL = 1
    LogColored.initialize()

    val jobs = if (args.getOrNull(0) == "-j") args.getOrNull(1)?.toInt()
            ?: { throw IllegalArgumentException("invalid arguments: ${args.joinToString()}") }.invoke()
    else NUM_PARALLEL

    LanternaConsole().scope {
        outnl(BLUE_BRIGHT("test flakyness with "), CYAN("$jobs"), BLUE_BRIGHT(if (jobs > 1) "parallel jobs" else "job" + "\n"))

        ResultPrinter(this).progress { channel -> (0 until jobs).map { channel.launchJob(it) }.joinAll() }
    }
    exitProcess(0).asUnit()
}
