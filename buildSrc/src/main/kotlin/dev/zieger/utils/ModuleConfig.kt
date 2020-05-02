package dev.zieger.utils

import dev.zieger.utils.ModuleType.ANDROID_LIB
import dev.zieger.utils.ModuleType.JVM_LIB
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
            JVM_LIB -> {
                coroutinesJdk
                testCoroutinesJdk
            }
            ANDROID_LIB -> {
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


        mockk
        koinTest
        junitJupiter

        block()
    }
}