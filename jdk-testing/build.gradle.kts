import de.gapps.utils.ModuleType.JVM
import de.gapps.utils.configModule
import de.gapps.utils.jdk

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("jdk-testing", JVM) {
    jdk
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
