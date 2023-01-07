plugins {
    id("java")
    id("org.gradle.java-library")
    id("kotlin")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("org.gradle.jacoco")
}

dependencies {
    val koinVersion: String by project
    implementation("io.insert-koin:koin-core:$koinVersion")

    val koTestVersion: String by project
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$koTestVersion")
    testImplementation("io.kotest:kotest-property:$koTestVersion")
    val mockkVersion: String by project
    testImplementation("io.mockk:mockk:$mockkVersion")
}