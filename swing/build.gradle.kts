import dev.zieger.utils.ModuleType.JVM_LIB
import dev.zieger.utils.configModule
import dev.zieger.utils.core
import dev.zieger.utils.coroutinesSwing

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