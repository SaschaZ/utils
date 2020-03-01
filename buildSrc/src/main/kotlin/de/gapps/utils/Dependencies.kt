//package de.gapps.utils
//
//import org.gradle.api.artifacts.dsl.DependencyHandler
//import org.gradle.kotlin.dsl.DependencyHandlerScope
//
//fun DependencyHandlerScope.kotlinJunit5() {
//       coreTesting
//
//        coroutinesAndroid
//        coroutinesCore
//
//        mockk
//        koinTest
//
//        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.6.0")
//}
//
//fun DependencyHandler.androidTesting() {
//    Libs.apply {
//        add("testImplementation", coroutinesAndroid)
//        add("testImplementation", "io.kotlintest:kotlintest-runner-junit5:${Versions.kotest}")
//        add("testImplementation", androidXtestCore)
//        add("testImplementation", androidXtestRules)
//        add("testImplementation", androidXtestRunner)
//        add("testImplementation", androidXtestEspressoCore)
//        add("testImplementation", androidXtestExt)
//    }
//}