import de.gapps.utils.ModuleType.JVM
import de.gapps.utils.configModule
import de.gapps.utils.core
import de.gapps.utils.mockWebServer

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