import dev.zieger.utils.ModuleType.JVM
import dev.zieger.utils.configModule
import dev.zieger.utils.jdk

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
