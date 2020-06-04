import dev.zieger.utils.ModuleType.JVM_LIB
import dev.zieger.utils.Versions
import dev.zieger.utils.configModule
import dev.zieger.utils.coreTesting
import dev.zieger.utils.kotlinReflect

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
    jacoco
}

jacoco {
    toolVersion = Versions.jacoco
    reportsDir = file("$buildDir/jacoco")
}

tasks.register<JacocoReport>("applicationCodeCoverageReport") {
    sourceSets(sourceSets.main.get())
}

configModule("core", JVM_LIB) {
    coreTesting
    kotlinReflect
}

tasks {
    test {
        useJUnitPlatform()
        outputs.upToDateWhen { false }

        testLogging {
//            events = setOf(
//                TestLogEvent.FAILED,
//                TestLogEvent.PASSED,
//                TestLogEvent.SKIPPED,
//                TestLogEvent.STANDARD_ERROR,
//                TestLogEvent.STANDARD_OUT
//            )
//            exceptionFormat = TestExceptionFormat.FULL
//            showExceptions = true
//            showCauses = true
//            showStackTraces = true
//
//            debug {
//                events = setOf(
//                    TestLogEvent.STARTED,
//                    TestLogEvent.FAILED,
//                    TestLogEvent.PASSED,
//                    TestLogEvent.SKIPPED,
//                    TestLogEvent.STANDARD_ERROR,
//                    TestLogEvent.STANDARD_OUT
//                )
//                exceptionFormat = TestExceptionFormat.FULL
//            }
//            info.events = debug.events
//            info.exceptionFormat = debug.exceptionFormat

//            afterSuite(object : groovy.lang.Closure<Any>(this) {
//                override fun call(vararg args: Any?): Any {
//                    return super.call(*args)
//                }
//            })
//            { desc, result ->
//                if (!desc.parent) { // will match the outermost suite
//                    val output =
//                        "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
//                    val startItem = '|  '
//                    val endItem = '  |'
//                    val repeatLength = startItem.length() + output.length() + endItem.length()
//                    println('\n' + ('-' * repeatLength) + '\n' + startItem + output + endItem + '\n' + ('-' * repeatLength))
//                }
//            }
        }
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.destination = file("${buildDir}/jacoco")
        }
    }
}
