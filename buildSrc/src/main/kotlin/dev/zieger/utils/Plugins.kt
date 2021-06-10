package dev.zieger.utils

import org.gradle.api.Plugin

fun ProjectContext.configPlugins(): Plugin<Any> =
    plugins.run {
        apply("java")
        when (isAndroid) {
            true -> {
                apply("com.android.application")
                apply("kotlin-android")
                apply("kotlin-android-extensions")
            }
            false -> {
                apply("kotlin")
            }
        }
        apply("kotlinx-serialization")
        apply("maven-publish")
//        apply("org.jetbrains.dokka")
//   y     apply("org.gradle.jacoco")
    }