import dev.zieger.utils.*
import dev.zieger.utils.ModuleType.JVM_LIB

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("core-testing", JVM_LIB) {
    core
    coroutinesTest
    mockWebServer
    kotlinReflect
}

tasks {
    test {
        useJUnitPlatform()
        outputs.upToDateWhen {false}
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}