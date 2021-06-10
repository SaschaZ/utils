package dev.zieger.utils

import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.kotlin.dsl.dependencies

fun ProjectContext.configDependencies() = dependencies {
    when (isAndroid) {
        true -> {
            coroutinesAndroid
            testCoroutinesAndroid
        }
        false -> {
            coroutinesJdk
            testCoroutinesJdk
        }
    }

    kotlin
    kotlinSerialization

//    koin


    koTestRunner
    koTestAssertions
    koTestProperty

    mockk
//    koinTest

    add("testImplementation", project(":$name"))
}