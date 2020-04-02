@file:Suppress("SpellCheckingInspection")

package dev.zieger.utils

import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

val DependencyHandlerScope.core
    get() = "implementation"(project(":core"))
val DependencyHandlerScope.coreTesting
    get() = "testImplementation"(project(":core-testing"))
val DependencyHandlerScope.jdk
    get() = "implementation"(project(":jdk"))
val DependencyHandlerScope.jdkTesting
    get() = "testImplementation"(project(":jdk-testing"))
val DependencyHandlerScope.android
    get() = "implementation"(project(":android"))
val DependencyHandlerScope.androidTesting
    get() = "testImplementation"(project(":android-testing"))

val DependencyHandlerScope.kotlin
    get() = "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
val DependencyHandlerScope.kotlinTest
    get() = "implementation"("org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}")

val DependencyHandlerScope.coroutinesCore
    get() = "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
val DependencyHandlerScope.coroutinesJdk
    get() = "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}")
val DependencyHandlerScope.testCoroutinesCore
    get() = "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
val DependencyHandlerScope.testCoroutinesJdk
    get() = "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}")
val DependencyHandlerScope.coroutinesAndroid
    get() = "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
val DependencyHandlerScope.testCoroutinesAndroid
    get() = "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
val DependencyHandlerScope.coroutinesSwing
    get() = "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.coroutines}")

val DependencyHandlerScope.multidex
    get() = "implementation"("androidx.multidex:multidex:${Versions.multidex}")
val DependencyHandlerScope.androidXappCompat
    get() = "implementation"("androidx.appcompat:appcompat:${Versions.androidXappCompat}")
val DependencyHandlerScope.androidXcoreKtx
    get() = "implementation"("androidx.core:core-ktx:${Versions.androidXcoreKtx}")
val DependencyHandlerScope.androidXconstraintLayout
    get() = "implementation"("androidx.constraintlayout:constraintlayout:${Versions.androidXconstraintLayout}")

val DependencyHandlerScope.koin
    get() = "implementation"("org.koin:koin-core:2.0.1")
val DependencyHandlerScope.koinTest
    get() = "implementation"("org.koin:koin-test:${Versions.koin}")

val DependencyHandlerScope.jackson
    get() = "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
val DependencyHandlerScope.jacksonDatabind
    get() = "implementation"("com.fasterxml.jackson.core:jackson-databind:2.10.3")
val DependencyHandlerScope.slf4jSimple
    get() = "implementation"("org.slf4j:slf4j-simple:1.7.29")
val DependencyHandlerScope.mordant
    get() = "implementation"("com.github.ajalt:mordant:1.2.1")
val DependencyHandlerScope.progressbar
    get() = "implementation"("me.tongfei:progressbar:0.8.0")


val DependencyHandlerScope.androidXtestEspressoCore
    get() = "testImplementation"("androidx.test.espresso:espresso-core:${Versions.androidXtestEspressoCore}")
val DependencyHandlerScope.androidXtestRunner
    get() = "testImplementation"("androidx.test:runner:${Versions.androidXtestRunner}")
val DependencyHandlerScope.androidXtestCore
    get() = "testImplementation"("androidx.test:core:${Versions.androidXtestCore}")
val DependencyHandlerScope.androidXtestRules
    get() = "testImplementation"("androidx.test:rules:${Versions.androidXtestRules}")
val DependencyHandlerScope.androidXtestExt
    get() = "testImplementation"("androidx.test.ext:junit:${Versions.androidXtestExt}")

val DependencyHandlerScope.mockWebServer
    get() = "implementation"("com.squareup.okhttp3:mockwebserver:${Versions.mockWebServer}")
val DependencyHandlerScope.bouncyCastle
    get() = "implementation"("org.bouncycastle:bcprov-jdk16:${Versions.bouncyCastle}")

val DependencyHandlerScope.mockk
    get() = "testImplementation"("io.mockk:mockk:${Versions.mockk}")
val DependencyHandlerScope.junitJupiter
    get() = "testImplementation"("org.junit.jupiter:junit-jupiter:${Versions.junitJupiterEngine}")