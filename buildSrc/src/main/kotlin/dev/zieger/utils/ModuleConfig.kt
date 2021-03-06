package dev.zieger.utils

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
            ModuleType.JVM_LIB -> {
                coroutinesJdk
                testCoroutinesJdk
            }
            ModuleType.ANDROID_LIB -> {
                coroutinesAndroid
                testCoroutinesAndroid

                androidXappCompat
                androidXcoreKtx
                androidXconstraintLayout
            }
        }

        kotlin
        moshi
        moshiKotlin

        koin
        slf4jSimple


//        koTestRunner
//        koTestAssertions
//        koTestProperty

        junitJupiter
        mockk
        koinTest
        coroutinesTest

        block()
    }
}