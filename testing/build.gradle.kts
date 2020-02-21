import de.gapps.utils.LibraryType.JAR
import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka") version "0.10.0"
}

dependencies {
    Libs.run {
        api(core)

        implementation(kotlin)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
        implementation("org.jetbrains.kotlin:kotlin-test:1.3.61")

        implementation("com.squareup.okhttp3:mockwebserver:4.3.1")
        implementation("org.bouncycastle:bcprov-jdk16:1.46")
        implementation("androidx.test:rules:1.2.0")
        implementation("androidx.test:runner:1.2.0")
        implementation("androidx.test.espresso:espresso-core:3.2.0")
        implementation("androidx.test:core:1.2.0")

        testImplementation(project(":core"))
        testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    }
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

configurePublishing(JAR, "testing")