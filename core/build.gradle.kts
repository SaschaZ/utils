import de.gapps.utils.LibraryType.JAR
import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka") version "0.10.0"
}

dependencies {
    with(Libs) {
        implementation(kotlin)
        implementation(coroutinesAndroid)
        implementation(coroutinesSwing)

        implementation(koin)
        implementation(jackson)
        implementation(slf4jSimple)
    }

    with(Dependencies) { fullTesting() }
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

configurePublishing(JAR, "core")