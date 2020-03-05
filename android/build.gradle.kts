import de.gapps.utils.Android
import de.gapps.utils.ModuleType.ANDROID
import de.gapps.utils.configModule
import de.gapps.utils.core

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("de.mannodermaus.android-junit5")
}

configModule("android", ANDROID) {
    core
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

    sourceSets {
        getByName("main").java.setSrcDirs(listOf("src/main/kotlin"))
        getByName("main").assets.setSrcDirs(listOf("src/main/assets"))

        getByName("test").java.setSrcDirs(listOf("src/main/kotlin", "src/test/kotlin"))
        getByName("test").assets.setSrcDirs(listOf("src/main/assets"))
    }
}

tasks {
    // config JVM target to 1.8 for kotlin compilation tasks
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}