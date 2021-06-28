pluginManagement {
    val kotlinVersion: String by settings
    val composeVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.multiplatform") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.jetbrains.compose") version composeVersion
    }
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
