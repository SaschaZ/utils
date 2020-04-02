import dev.zieger.utils.ModuleType.JVM
import dev.zieger.utils.configModule
import dev.zieger.utils.core
import dev.zieger.utils.mockWebServer

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("core-testing", JVM) {
    core

    mockWebServer
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}