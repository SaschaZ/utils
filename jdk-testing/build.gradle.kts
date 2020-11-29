import dev.zieger.utils.ModuleType.JVM_LIB
import dev.zieger.utils.configModule
import dev.zieger.utils.jdk

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("jdk-testing", JVM_LIB) {
    jdk
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
