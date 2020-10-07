import dev.zieger.utils.Android
import dev.zieger.utils.ModuleType.ANDROID_LIB
import dev.zieger.utils.androidXrecyclerView
import dev.zieger.utils.configModule
import dev.zieger.utils.core

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("android", ANDROID_LIB) {
    core
    androidXrecyclerView

    "testImplementation"(project(":android"))
    "testImplementation"(project(":core-testing"))
}

android {
    compileSdkVersion(Android.apiLevel)
    buildToolsVersion(Android.buildTools)
    defaultConfig {
        minSdkVersion(Android.minSdk)
        targetSdkVersion(Android.targetSdk)
        versionCode = Android.versionCode
        versionName = Android.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    lintOptions {
        isAbortOnError = false
    }

    sourceSets {
        getByName("main").java.setSrcDirs(listOf("src/main/kotlin"))
        getByName("main").assets.setSrcDirs(listOf("src/main/assets"))

        getByName("test").java.setSrcDirs(listOf("src/main/kotlin", "src/test/kotlin"))
        getByName("test").assets.setSrcDirs(listOf("src/main/assets"))
    }
}