plugins {
    id("java")
    id("org.gradle.java-library")
    id("kotlin")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("org.gradle.jacoco")
}

dependencies {
    api(project(":coroutines"))
    api(project(":time"))
    api(project(":misc"))
    api(project(":globals"))
    api(project(":observables"))
    api(project(":statemachine"))
}
