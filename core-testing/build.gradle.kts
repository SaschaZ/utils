import dev.zieger.utils.ModuleType.JVM_LIB
import dev.zieger.utils.configModule
import dev.zieger.utils.core
import dev.zieger.utils.coroutinesTest
import dev.zieger.utils.mockWebServer

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