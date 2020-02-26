import de.gapps.utils.LibraryType.JAR
import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

dependencies {
    with(Libs) {
        implementation(kotlin)
        implementation(coroutinesJdk)

        implementation(koin)
        implementation(jackson)
        implementation(slf4jSimple)
    }

    with(Dependencies) { kotlinTesting() }
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

configurePublishing(JAR, "core")