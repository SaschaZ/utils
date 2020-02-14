@file:Suppress("SpellCheckingInspection")

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

object Libs  {
    val DependencyHandler.core
        get() = project(":core", "default")
    val DependencyHandler.console
        get() = project(":console")
    val DependencyHandler.testing
        get() = project(":testing")
    val DependencyHandler.channels
        get() = project(":channels")
    val DependencyHandler.android
        get() = project(":android")

    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlinTest = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"
    val kotlinTestRunnerJunit5 = "io.kotlintest:kotlintest-runner-junit5:${Versions.kotlinTestRunnerJunit5}"

    val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val coroutinesJdk = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}"
    val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    val coroutinesSwing = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.coroutines}"

    val multidex = "androidx.multidex:multidex:${Versions.multidex}"
    val androidXappCompat = "androidx.appcompat:appcompat:${Versions.androidXappCompat}"
    val androidXcoreKtx = "androidx.core:core-ktx:${Versions.androidXcoreKtx}"
    val androidXconstraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.androidXconstraintLayout}"

    val koin = "org.koin:koin-core:2.0.1"
    val koinTest = "org.koin:koin-test:${Versions.koin}"

    val jackson = "com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1"
    val slf4jSimple = "org.slf4j:slf4j-simple:1.7.29"
    val mordant = "com.github.ajalt:mordant:1.2.1"
    val progressbar = "me.tongfei:progressbar:0.8.0"



    val androidXtestEspressoCore = "androidx.test.espresso:espresso-core:${Versions.androidXtestEspressoCore}"
    val androidXtestRunner = "androidx.test:runner:${Versions.androidXtestRunner}"
    val androidXtestCore = "androidx.test:core:${Versions.androidXtestCore}"
    val androidXtestRules = "androidx.test:rules:${Versions.androidXtestRules}"
    val androidXtestExt = "androidx.test.ext:junit:${Versions.androidXtestExt}"

    val mockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.mockWebServer}"
    val bouncyCastle = "org.bouncycastle:bcprov-jdk16:${Versions.bouncyCastle}"

    val mockk = "io.mockk:mockk:${Versions.mockk}"
    val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiterEngine}"
}