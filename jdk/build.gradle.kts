import de.gapps.utils.ModuleType.JVM
import de.gapps.utils.configModule
import de.gapps.utils.core

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("jdk", JVM) {
    core
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