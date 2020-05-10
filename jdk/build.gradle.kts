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
}

tasks {
    test {
        useJUnitPlatform()
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}