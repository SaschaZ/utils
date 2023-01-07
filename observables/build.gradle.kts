
plugins {
    id("java")
    id("org.gradle.java-library")
    id("kotlin")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("org.gradle.jacoco")
}

dependencies {
    val kotlinCoroutinesVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    api(project(":time"))
    api(project(":misc"))
    api(project(":globals"))

    val koTestVersion: String by project
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$koTestVersion")
    testImplementation("io.kotest:kotest-property:$koTestVersion")
    val mockkVersion: String by project
    testImplementation("io.mockk:mockk:$mockkVersion")
}