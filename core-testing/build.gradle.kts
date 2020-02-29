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
        implementation(core)

        implementation(kotlin)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")

        implementation("com.squareup.okhttp3:mockwebserver:4.3.1")
        implementation("org.bouncycastle:bcprov-jdk16:1.46")

        with(Dependencies) { kotlinJunit5() }
    }
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

configurePublishing(JAR, "core-testing")