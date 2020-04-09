import de.gapps.utils.ModuleType.JVM_LIB
import de.gapps.utils.configModule
import de.gapps.utils.coreTesting
import de.gapps.utils.kotlinReflect

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.3.71"
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("core", JVM_LIB) {
    coreTesting
    kotlinReflect
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