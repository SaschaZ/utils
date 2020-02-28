package de.gapps.utils

import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    fun DependencyHandler.kotlinJunit5() {
        Libs.apply {
            add("testImplementation", coreTesting)

            add("testImplementation", coroutinesAndroid)
            add("testImplementation", coroutinesCore)
//            add("testImplementation", kotlinTes)

            add("testImplementation", mockk)
            add("testImplementation", koinTest)

            add("testImplementation", "io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
//            add("testImplementation", "org.junit.jupiter:junit-jupiter:5.6.0")
        }
    }

    fun DependencyHandler.androidTesting() {
        Libs.apply {
            add("testImplementation", coroutinesAndroid)
            add("testImplementation", "io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
            add("testImplementation", androidXtestCore)
            add("testImplementation", androidXtestRules)
            add("testImplementation", androidXtestRunner)
            add("testImplementation", androidXtestEspressoCore)
            add("testImplementation", androidXtestExt)
        }
    }
}