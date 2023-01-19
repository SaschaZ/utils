pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.gradle.java-library")
        id("org.jetbrains.dokka") version kotlinVersion
    }
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "utils"
include("coroutines")
include("globals")
include("log")
include("misc")
include("statemachine")
include("time")
include("observables")
include("utils")
