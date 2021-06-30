pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.multiplatform") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
    }
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "utils3"
include("console")
include("coroutines")
include("globals")
include("log")
include("misc")
include("time")
include("observables")
