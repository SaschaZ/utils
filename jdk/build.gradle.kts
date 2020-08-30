import dev.zieger.utils.*
import dev.zieger.utils.ModuleType.JVM_LIB

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("jdk", JVM_LIB) {
    core
    mordant
    progressbar
    coroutinesSwing
    lanterna
}

tasks {
    test {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}