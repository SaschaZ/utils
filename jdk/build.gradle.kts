import dev.zieger.utils.ModuleType.JVM_LIB
import dev.zieger.utils.configModule
import dev.zieger.utils.core
import dev.zieger.utils.mordant
import dev.zieger.utils.progressbar

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("jdk", JVM_LIB) {
    core
    mordant
    progressbar
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