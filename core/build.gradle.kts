import de.gapps.utils.ModuleType.JVM
import de.gapps.utils.configModule
import de.gapps.utils.coreTesting

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("core", JVM) {
    coreTesting

    testImplementation("org.hamcrest:hamcrest-core:2.1")
    testImplementation("org.hamcrest:hamcrest-library:2.1")
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