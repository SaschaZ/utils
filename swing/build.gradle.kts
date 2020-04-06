import de.gapps.utils.ModuleType.JVM_LIB
import de.gapps.utils.configModule
import de.gapps.utils.core
import de.gapps.utils.coroutinesSwing

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("swing", JVM_LIB) {
    core
    coroutinesSwing
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