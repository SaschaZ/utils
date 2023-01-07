pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.gradle.java-library")

        val dokkaVersion: String by settings
        id("org.jetbrains.dokka") version dokkaVersion

        id("org.gradle.jacoco")
    }
    repositories {
        gradlePluginPortal()
        maven(url = "https://dl.bintray.com/kotlin/dokka")
    }
}

rootProject.name = "utils"
include("coroutines")
include("globals")
include("koin")
include("log")
include("misc")
include("statemachine")
include("time")
include("observables")
include("utils")
