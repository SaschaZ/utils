pluginManagement {
    buildscript {
        repositories {
            google() // <-- here
            jcenter()
//            maven {
//                url = "https://maven.google.com/"
//                name = "Google"
//            }
        }
    }
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
