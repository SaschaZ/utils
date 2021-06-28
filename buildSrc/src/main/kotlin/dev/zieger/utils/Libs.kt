@file:Suppress("SpellCheckingInspection", "unused")

package dev.zieger.utils

import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project


val DependencyHandlerScope.coroutines
    get() = add("implementation", project(":coroutines"))
val DependencyHandlerScope.globals
    get() = add("implementation", project(":globals"))
val DependencyHandlerScope.log
    get() = add("implementation", project(":log"))
val DependencyHandlerScope.misc
    get() = add("implementation", project(":misc"))
val DependencyHandlerScope.observables
    get() = add("implementation", project(":observables"))
val DependencyHandlerScope.time
    get() = add("implementation", project(":time"))
val DependencyHandlerScope.testTime
    get() = add("testImplementation", project(":time"))

val DependencyHandlerScope.kotlin
    get() = add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlinVersion}")
val DependencyHandlerScope.kotlinReflect
    get() = add("implementation", "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}")

val DependencyHandlerScope.coroutinesCore
    get() = add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutinesVersion}")
val DependencyHandlerScope.coroutinesJdk
    get() = add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesVersion}")
val DependencyHandlerScope.testCoroutinesCore
    get() = add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutinesVersion}")
val DependencyHandlerScope.testCoroutinesJdk
    get() = add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesVersion}")
val DependencyHandlerScope.coroutinesAndroid
    get() = add("implementation","org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutinesVersion}")
val DependencyHandlerScope.testCoroutinesAndroid
    get() = add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutinesVersion}")
val DependencyHandlerScope.coroutinesSwing
    get() = add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.kotlinCoroutinesVersion}")

val DependencyHandlerScope.kotlinSerialization
    get() = add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerializationVersion}")

val DependencyHandlerScope.koin
    get() = add("implementation","org.koin:koin-core:${Versions.koinVersion}")
val DependencyHandlerScope.koinTest
    get() = add("implementation","org.koin:koin-test:${Versions.koinVersion}")

val DependencyHandlerScope.mockk
    get() = add( "testImplementation","io.mockk:mockk:${Versions.mockkVersion}")

val DependencyHandlerScope.ktorClientGson
    get() = add("implementation", "io.ktor:ktor-client-gson:${Versions.ktorVersion}")
val DependencyHandlerScope.ktorServerGson
    get() = add("implementation", "io.ktor:ktor-jackson:${Versions.ktorVersion}")

val DependencyHandlerScope.lanterna
    get() = add("implementation","com.googlecode.lanterna:lanterna:${Versions.lanternaVersion}")

val DependencyHandlerScope.koTestRunner
    get() = add("testImplementation", "io.kotest:kotest-runner-junit5-jvm:${Versions.kotestVersion}")
val DependencyHandlerScope.koTestAssertions
    get() = add("testImplementation", "io.kotest:kotest-assertions-core-jvm:${Versions.kotestVersion}")
val DependencyHandlerScope.koTestProperty
    get() = add("testImplementation", "io.kotest:kotest-property-jvm:${Versions.kotestVersion}")