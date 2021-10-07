pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
    }
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "utils"
include("console")
include("coroutines")
include("globals")
include("koin")
include("log")
include("misc")
include("statemachine")
include("time")
include("observables")
