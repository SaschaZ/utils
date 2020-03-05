package de.gapps.utils

import de.gapps.utils.ModuleType.ANDROID
import de.gapps.utils.ModuleType.JVM
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

fun PluginDependenciesSpecScope.importPlugins(type: ModuleType) {
    when (type) {
        JVM -> kotlin("jvm")
        ANDROID -> {
            id("com.android.library")
            kotlin("android")
            id("kotlin-android-extensions")
        }
    }
    id("maven-publish")
    id("org.jetbrains.dokka")
}

fun Project.configModule(
    name: String,
    type: ModuleType,
    block: DependencyHandlerScope.() -> Unit = {}
) {
    configureDependencies(type, block)
    configurePublishing(type, name)
}

private fun Project.configureDependencies(type: ModuleType, block: DependencyHandlerScope.() -> Unit) {
    dependencies {
        when (type) {
            JVM -> {
                coroutinesJdk
                testCoroutinesJdk
            }
            ANDROID -> {
                coroutinesAndroid
                testCoroutinesAndroid

                androidXappCompat
                androidXcoreKtx
                androidXconstraintLayout
            }
        }

        kotlin

        koin
        jackson
        slf4jSimple


        mockk
        koinTest
        junitJupiter

        block()
    }
}