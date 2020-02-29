import de.gapps.utils.LibraryType.JAR
import de.gapps.utils.configurePublishing
import de.gapps.utils.Android
import de.gapps.utils.Libs
import de.gapps.utils.Dependencies

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

dependencies {
    Libs.run {
        implementation(kotlin)
        implementation(coroutinesJdk)
    }

    with(Dependencies) { kotlinJunit5() }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// config JVM target to 1.8 for kotlin compilation tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

configurePublishing(JAR, "jdk-testing")
