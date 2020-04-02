package dev.zieger.utils

import dev.zieger.utils.ModuleType.ANDROID
import dev.zieger.utils.ModuleType.JVM
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies

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